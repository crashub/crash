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
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.CompilationFailedException;
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
  private final Map<String, Closure> closures;

  private Closure getClosure(String name) {
    Closure closure = closures.get(name);

    //
    if (closure == null) {
      String id = "/commands/" + name + ".groovy";
      String script = context.loadScript(id);
      if (script != null) {
        closure = (Closure)groovyShell.evaluate(script, id);
        closures.put(name, closure);
      }
      else {
        // System.out.println("Could not load command with name " + name);
      }
    }

    //
    return closure;
  }

  public GroovyShell getGroovyShell() {
    return groovyShell;
  }

  public Object getAttribute(String name) {
    return groovyShell.getVariable(name);
  }

  public void setAttribute(String name, Object value) {
    groovyShell.setVariable(name, value);
  }

  Shell(final ShellContext context, final Map<String, Command> commands) {
    final StringWriter out = new StringWriter();
    Binding binding = new Binding() {
      @Override
      public Object getVariable(String name) {
        Closure closure = getClosure(name);

        //
        //
        if (closure != null) {
          return closure;
        }

        //
        try {
          return super.getVariable(name);
        }
        catch (MissingPropertyException e) {
          return null;
        }
      }

      @Override
      public Object getProperty(String property) {
        if ("out".equals(property)) {
          return out;
        }

        //
        return super.getProperty(property);
      }
    };

    //
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), binding);
    HashMap<String, Closure> closures = new HashMap<String, Closure>();

    //
    for (Map.Entry<String, Command> entry : commands.entrySet()) {
      String name = entry.getKey();
      Command command = entry.getValue();
      if (command instanceof JavaCommand) {
        final JavaCommand javaCommand = (JavaCommand)command;
        Closure closure = new Closure(this) {
          public String call(Object... o) {
            return javaCommand.call(o);
          }
        };
        closures.put(name, closure);
      }
      else {
        try {
          GroovyCommand groovyCommand = (GroovyCommand)command;
          Closure commandClosure = (Closure)groovyShell.evaluate(groovyCommand.getClosureText());
          closures.put(name, commandClosure);
        }
        catch (CompilationFailedException e) {
          System.out.println("Could not compile command " + name);
          e.printStackTrace(System.out);
        }
      }
    }

    // Evaluate login script
    String script = context.loadScript("/login.groovy");
    groovyShell.evaluate(script, "/login.groovy");

    //
    this.out = out.getBuffer();
    this.groovyShell = groovyShell;
    this.closures = closures;
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
    String[] chunks = s.split("\\s+");

    //
    if (chunks.length > 0) {
      // Rewrite
      StringBuilder builder = new StringBuilder();
      builder.append(chunks[0]).append('(');
      for (int i = 1; i < chunks.length; i++) {
        if (i > 1) {
          builder.append(',');
        }
        String chunk = chunks[i];
        builder.append('\'').append(chunk).append('\'');
      }
      builder.append(')');
      s = builder.toString();

      //
      Object o = null;
      try {
        o = groovyShell.evaluate(s);
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

    //
    return null;
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
