/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.connector.AbstractShell;
import org.crsh.display.SimpleDisplayContext;
import org.crsh.display.structure.Element;
import org.crsh.jcr.NodeMetaClass;
import org.crsh.util.CompletionHandler;
import org.crsh.util.ImmediateFuture;
import org.crsh.util.TimestampedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSH implements AbstractShell<ShellResponse> {

  static {
    // Force integration of node meta class
    NodeMetaClass.setup();
  }

  /** . */
  private static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final GroovyShell groovyShell;

  /** . */
  private final StringBuffer out;

  /** . */
  private final ShellContext context;

  /** . */
  private final Map<String, TimestampedObject<Class<ShellCommand>>> commands;

  /** . */
  final CommandContext commandContext;

  /** . */
  private final ExecutorService executor;

  ShellCommand getClosure(String name) {
    TimestampedObject<Class<ShellCommand>> closure = commands.get(name);

    //
    String id = "/groovy/commands/" + name + ".groovy";
    Resource script = context.loadResource(id);

    //
    if (script != null) {
      if (closure != null) {
        if (script.getTimestamp() != closure.getTimestamp()) {
          closure = null;
        }
      }

      //
      if (closure == null) {
        Class<?> clazz = groovyShell.getClassLoader().parseClass(script.getContent(), id);
        if (ShellCommand.class.isAssignableFrom(clazz)) {
          Class<ShellCommand> commandClazz = (Class<ShellCommand>) clazz;
          closure = biltooo(script.getTimestamp(), commandClazz);
          commands.put(name, closure);
        } else {
          log.error("Parsed script does not implements " + ShellCommand.class.getName());
        }
      }
    }

    //
    try {
      return closure.getObject().newInstance();
    }
    catch (InstantiationException e) {
      throw new Error(e);
    }
    catch (IllegalAccessException e) {
      throw new Error(e);
    }
  }

  private <T extends ShellCommand> TimestampedObject<Class<T>> biltooo(long timestamp, Class<T> aaa) {
    return new TimestampedObject<Class<T>>(timestamp, aaa);
  }

  public GroovyShell getGroovyShell() {
    return groovyShell;
  }

  public Object getAttribute(String name) {
    return commandContext.get(name);
  }

  public void setAttribute(String name, Object value) {
    commandContext.put(name, value);
  }

  public CRaSH(ShellContext context) {
    this(context, null);
  }

  CRaSH(final ShellContext context, ExecutorService executor) {
    CommandContext commandContext = new CommandContext();

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(ScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(commandContext), config);


    // Evaluate login script
    String script = context.loadResource("/groovy/login.groovy").getContent();
    groovyShell.evaluate(script, "/groovy/login.groovy");

    //
    this.commandContext = commandContext;
    this.out = new StringBuffer();
    this.groovyShell = groovyShell;
    this.commands = new ConcurrentHashMap<String, TimestampedObject<Class<ShellCommand>>>();
    this.context = context;
    this.executor = executor;
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadResource("/groovy/logout.groovy").getContent();
    groovyShell.evaluate(script, "/groovy/logout.groovy");
  }

  public ShellResponse evaluate(String s) {
    Evaluable evaluable = new Evaluable(this, s, null);
    return evaluable.call();
  }

  public Future<ShellResponse> submitEvaluation(String s, CompletionHandler<ShellResponse> handler) {
    Evaluable callable = new Evaluable(this, s, handler);
    if (executor != null) {
      log.debug("Submitting to executor");
      return executor.submit(callable);
    } else {
      ShellResponse response = callable.call();
      return new ImmediateFuture<ShellResponse>(response);
    }
  }

  // Shell implementation **********************************************************************************************

  public ShellResponse okResponse() {
    return new ShellResponse.Ok();
  }

  public void doClose() {
    close();
  }

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
  }

  public Future<ShellResponse> doSubmitEvaluation(String request, CompletionHandler<ShellResponse> responseHandler) {
    return submitEvaluation(request, responseHandler);
  }

  public String decode(ShellResponse response) {
    String ret = null;
    try {
      String result = null;
      if (response instanceof ShellResponse.Error) {
        ShellResponse.Error error = (ShellResponse.Error) response;
        Throwable t = error.getThrowable();
        if (t instanceof Error) {
          throw ((Error) t);
        } else if (t instanceof ScriptException) {
          result = "Error: " + t.getMessage();
        } else if (t instanceof RuntimeException) {
          result = "Unexpected exception: " + t.getMessage();
          t.printStackTrace(System.err);
        } else if (t instanceof Exception) {
          result = "Unexpected exception: " + t.getMessage();
          t.printStackTrace(System.err);
        } else {
          result = "Unexpected throwable: " + t.getMessage();
          t.printStackTrace(System.err);
        }
      } else if (response instanceof ShellResponse.Ok) {

        if (response instanceof ShellResponse.Display) {
          ShellResponse.Display display = (ShellResponse.Display) response;
          SimpleDisplayContext context = new SimpleDisplayContext("\r\n");
          for (Element element : display) {
            element.print(context);
          }
          result = context.getText();
        } else {
          result = "";
        }
      } else if (response instanceof ShellResponse.NoCommand) {
        result = "Please type something";
      } else if (response instanceof ShellResponse.UnkownCommand) {
        ShellResponse.UnkownCommand unknown = (ShellResponse.UnkownCommand) response;
        result = "Unknown command " + unknown.getName();
      }

      // Format response if any
      if (result != null) {
        ret = "" + String.valueOf(result) + "\r\n";
      }
    } catch (Throwable t) {
      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);
      printer.print("ERROR: ");
      t.printStackTrace(printer);
      printer.println();
      printer.close();
      ret = writer.toString();
    }

    //
    // NEED TO ENABLE THIS AGAIN
/*
    if (isClosed()) {
      ret += "Have a good day!\r\n";
    }
*/

    //
    if (ret == null) {
      ret = getPrompt();
    } else {
      ret += getPrompt();
    }

    //
    return ret;
  }
}
