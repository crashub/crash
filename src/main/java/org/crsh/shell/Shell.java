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
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.console.ConsoleBuilder;
import org.crsh.console.ConsoleElement;
import org.crsh.console.MessageElement;
import org.crsh.jcr.NodeMetaClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private final GroovyShell groovyShell;

  /** . */
  private final StringBuffer out;

  /** . */
  private final ShellContext context;

  /** . */
  private final Map<String, Class<? extends ShellCommand>> commands;

  /** . */
  private final CommandContext commandContext;

  private ShellCommand getClosure(String name) {
    Class<? extends ShellCommand> closure = commands.get(name);

    //
    if (closure == null) {
      String id = "/commands/" + name + ".groovy";
      String script = context.loadScript(id);
      if (script != null) {
        Class<?> clazz = groovyShell.getClassLoader().parseClass(script, id);
        if (ShellCommand.class.isAssignableFrom(clazz)) {
          closure = clazz.asSubclass(ShellCommand.class);
          commands.put(name, closure);
        } else {
          System.out.println("Parsed script does not implements " + ShellCommand.class.getName());
        }
      }
      else {
        return null;
      }
    }

    //
    try {
      return closure.newInstance();
    }
    catch (InstantiationException e) {
      throw new Error(e);
    }
    catch (IllegalAccessException e) {
      throw new Error(e);
    }
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

  Shell(final ShellContext context) {
    CommandContext commandContext = new CommandContext();

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(ScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(commandContext), config);


    // Evaluate login script
    String script = context.loadScript("/login.groovy");
    groovyShell.evaluate(script, "/login.groovy");

    //
    this.commandContext = commandContext;
    this.out = new StringBuffer();
    this.groovyShell = groovyShell;
    this.commands = new HashMap<String, Class<? extends ShellCommand>>();
    this.context = context;
  }

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadScript("/logout.groovy");
    groovyShell.evaluate(script, "/logout.groovy");
  }

  public List<ConsoleElement> evaluate2(String s) {

    // Trim
    s = s.trim();

    //
    Object o = null;
    if (s.length() > 0) {

      // We'll have at least one chunk
      String[] chunks = s.trim().split("\\s+");


      // Get command
      ShellCommand cmd = getClosure(chunks[0]);

      //
      if (cmd != null) {
        // Build args
        String[] args = new String[chunks.length - 1];
        System.arraycopy(chunks, 1, args, 0, args.length);

        //
        try {
          o = cmd.execute(commandContext, args);
        }
        catch (Throwable t) {
          if (t instanceof ScriptException) {
            o = "Error: " + t.getMessage();
          }
          else if (o instanceof RuntimeException) {
            o = "Unexpected exception: " + t.getMessage();
            t.printStackTrace(System.err);
          }
          else if (t instanceof Exception) {
            o = "Unexpected exception: " + t.getMessage();
            t.printStackTrace(System.err);
          }
          else if (t instanceof Error) {
            throw ((Error)t);
          }
          else {
            o = "Unexpected throwable: " + t.getMessage();
            t.printStackTrace(System.err);
          }
        }
      } else {
        o = "Unknown command " + chunks[0];
      }
    } else {
      o = "Please type something";
    }

    //
    if (o instanceof ConsoleBuilder) {
      return ((ConsoleBuilder)o).getElements();
    }
    else if (o != null) {
      return Collections.<ConsoleElement>singletonList(new MessageElement(o.toString()));
    }
    else {
      return Collections.emptyList();
    }
  }

  public String evaluate(String s) {
    List<ConsoleElement> elements = evaluate2(s);


    if (elements != null) {
      StringWriter writer = new StringWriter();
      if (out.length() > 0) {
        writer.append(out);
        out.setLength(0);
      }

      //
      PrintWriter printer = new PrintWriter(writer);
      for (ConsoleElement element : elements) {
        try {
          element.print(printer);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      //
      return writer.toString();
    }
    else {
      return null;
    }
  }
}
