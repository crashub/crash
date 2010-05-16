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
import org.crsh.jcr.NodeMetaClass;
import org.crsh.util.CompletionHandler;
import org.crsh.util.ImmediateFuture;
import org.crsh.util.TimestampedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Shell {

  static {
    // Force integration of node meta class
    NodeMetaClass.setup();
  }

  /** . */
  private static final Logger log = LoggerFactory.getLogger(Shell.class);

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

  public Shell(ShellContext context) {
    this(context, null);
  }

  Shell(final ShellContext context, ExecutorService executor) {
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

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
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
}
