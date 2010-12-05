/*
 * Copyright (C) 2010 eXo Platform SAS.
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
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TypeResolver;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class BaseCommand<C, P> extends GroovyObjectSupport implements ShellCommand<C, P> {

  /** . */
  private CommandContext<C, P> context;

  /** . */
  private boolean unquoteArguments;

  /** . */
  private Class<C> consumedType;

  /** . */
  private Class<P> producedType;

  protected BaseCommand() {
    this.context = null;
    this.unquoteArguments = true;
    this.consumedType = (Class<C>)TypeResolver.resolve(getClass(), ShellCommand.class, 0);
    this.producedType = (Class<P>)TypeResolver.resolve(getClass(), ShellCommand.class, 1);
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public Class<C> getConsumedType() {
    return consumedType;
  }

  @Override
  public final Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      Object o = context.getAttributes().get(name);
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
      return context.getAttributes().get(property);
    }
  }

  @Override
  public final void setProperty(String property, Object newValue) {
    try {
      super.setProperty(property, newValue);
    }
    catch (MissingPropertyException e) {
      context.getAttributes().put(property, newValue);
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

  public final void execute(CommandContext<C, P> context, String... args) throws ScriptException {
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
      ShellPrinter out = context.getWriter();

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
        execute(context);
      }
      finally {
        this.context = null;
      }
    }
  }

  protected abstract void execute(CommandContext<C, P> context) throws ScriptException;

}
