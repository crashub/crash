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
package org.crsh.shell.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.ShellCommand;
import org.crsh.jcr.NodeMetaClass;
import org.crsh.shell.*;
import org.crsh.util.TimestampedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSH implements Shell {

  static {
    // Force integration of node meta class
    NodeMetaClass.setup();
  }

  /** . */
  private static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final GroovyShell groovyShell;

  /** . */
  private final ShellContext context;

  /** . */
  private final Map<String, TimestampedObject<Class<ShellCommand>>> commands;

  /** . */
  final Map<String, Object> attributes;

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
    if (closure == null) {
      return null;
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
    return attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  public CRaSH(final ShellContext context) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    // Set version number
    attributes.put("version", context.getVersion());

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(attributes), config);

    // Evaluate login script
    String script = context.loadResource("/groovy/login.groovy").getContent();
    groovyShell.evaluate(script, "/groovy/login.groovy");

    //
    this.attributes = attributes;
    this.groovyShell = groovyShell;
    this.commands = new ConcurrentHashMap<String, TimestampedObject<Class<ShellCommand>>>();
    this.context = context;
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadResource("/groovy/logout.groovy").getContent();
    groovyShell.evaluate(script, "/groovy/logout.groovy");
  }

  public ShellResponse evaluate(String request) {
    return evaluate(request, null);
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return (String)groovyShell.evaluate("welcome();");
  }

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
  }

  public ShellResponse evaluate(String request, ShellResponseContext responseContext) {
    // Trim
    String s2 = request.trim();

    //
    log.debug("Invoking command " + s2);

    //
    ShellResponse response;
    if (s2.length() > 0) {
      try {
        // We'll have at least one chunk
        List<String> chunks = LineFormat.format(s2);

        // Get command
        ShellCommand cmd = getClosure(chunks.get(0));

        //
        if (cmd != null) {
          CommandContextImpl ctx = new CommandContextImpl(responseContext, attributes);

          // Build args
          String[] args = new String[chunks.size() - 1];
          chunks.subList(1, chunks.size()).toArray(args);
          cmd.execute(ctx, args);
          if (ctx.getBuffer() != null) {
            response = new ShellResponse.Display(ctx.getBuffer().toString());
          } else {
            response = new ShellResponse.Ok();
          }
        } else {
          response = new ShellResponse.UnkownCommand(chunks.get(0));
        }
      }
      catch (Throwable t) {
        response = new ShellResponse.Error(ErrorType.EVALUATION, t);
      }
    } else {
      response = new ShellResponse.NoCommand();
    }

    //
    return response;
  }
}
