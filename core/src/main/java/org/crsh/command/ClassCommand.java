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
package org.crsh.command;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ClassCommand extends GroovyObjectSupport implements ShellCommand {

  /** . */
  private CommandContext context;

  /** . */
  private boolean unquoteArguments;

  protected ClassCommand() {
    this.context = null;
    this.unquoteArguments = true;
  }

  @Override
  public final Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      Object o = context.get(name);
      if (o instanceof Closure) {
        Closure closure = (Closure)o;
        if (args instanceof Object[]) {
          Object[] array = (Object[])args;
          if (array.length == 0) {
            return closure.call();
          } else {
            return closure.call(array);
          }
        } else {
          return closure.call(args);
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public final Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return context.get(property);
    }
  }

  @Override
  public final void setProperty(String property, Object newValue) {
    try {
      super.setProperty(property, newValue);
    }
    catch (MissingPropertyException e) {
      context.put(property, newValue);
    }
  }

  /**
   * Returns true if the command wants its arguments to be unquoted.
   *
   * @return true if arguments must be unquoted
   */
  protected boolean getUnquoteArguments() {
    return unquoteArguments;
  }

  public void setUnquoteArguments(boolean unquoteArguments) {
    this.unquoteArguments = unquoteArguments;
  }

  protected final String readLine(String msg) {
    return readLine(msg, true);
  }

  protected final String readLine(String msg, boolean echo) {
    if (context == null) {
      throw new IllegalStateException("No current context");
    }
    return context.readLine(msg, echo);
  }

  public final void execute(CommandContext context, String... args) throws ScriptException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (args == null) {
      throw new NullPointerException();
    }

    //
    CmdLineParser parser = new CmdLineParser(this);

    //
    if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
      ShellWriter out = context.getWriter();

      //
      Description description = getClass().getAnnotation(Description.class);
      if (description != null) {
        out.write(description.value());
        out.write("\n");
      }

      //
      parser.printUsage(out, null);
    } else {
      // Remove surrounding quotes if there are
      if (unquoteArguments) {
        String[] foo = new String[args.length];
        for (int i = 0;i < args.length;i++) {
          String arg = args[i];
          if (arg.charAt(0) == '\'') {
            if (arg.charAt(arg.length() - 1) == '\'') {
              arg = arg.substring(1, arg.length() - 1);
            }
          } else if (arg.charAt(0) == '"') {
            if (arg.charAt(arg.length() - 1) == '"') {
              arg = arg.substring(1, arg.length() - 1);
            }
          }
          foo[i] = arg;
        }
        args = foo;
      }

      //
      try {
        parser.parseArgument(args);
      }
      catch (CmdLineException e) {
        throw new ScriptException(e.getMessage(), e);
      }

      //
      try {
        this.context = context;

        //
        Object o = execute();

        //
        if (o != null) {
          context.getWriter().print(o);
        }
      }
      finally {
        this.context = null;
      }
    }
  }

  protected abstract Object execute() throws ScriptException;

}
