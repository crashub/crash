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

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.analyzer.Analyzer;
import org.crsh.cmdline.analyzer.InvocationContext;
import org.crsh.cmdline.analyzer.MethodMatch;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A real CRaSH command, the most powerful kind of command.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CRaSHCommand extends GroovyCommand implements ShellCommand {

  /** . */
  private CommandContext context;

  /** . */
  private boolean unquoteArguments;

  /** . */
  private final ClassDescriptor<?> descriptor;

  protected CRaSHCommand() throws IntrospectionException {
    this.context = null;
    this.unquoteArguments = true;
    this.descriptor = CommandDescriptor.create(getClass());
  }

  /**
   * Returns true if the command wants its arguments to be unquoted.
   *
   * @return true if arguments must be unquoted
   */
  public final boolean getUnquoteArguments() {
    return unquoteArguments;
  }

  public final void setUnquoteArguments(boolean unquoteArguments) {
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

  @Override
  protected final CommandContext<?, ?> getContext() {
    return context;
  }

  public CommandInvoker<?, ?> createInvoker(String... args) {
    if (args == null) {
      throw new NullPointerException();
    }
    
    // WTF
    Analyzer analyzer = new Analyzer("main", descriptor);
    StringBuilder s = new StringBuilder();
    for (String arg : args) {
      if (s.length() > 0) {
        s.append(" ");
      }
      s.append(arg);
    }

    //
    InvocationContext invocationContext = new InvocationContext();
    invocationContext.setAttribute(CommandContext.class, context);
    final MethodMatch match = (MethodMatch)analyzer.analyze(s.toString());

    //
    if (match != null) {
      return new CommandInvoker() {

        Class consumedType = Void.class;
        Class producedType = Void.class;

        {
          // Try to find a command context argument
          Method m = match.getDescriptor().getMethod();

          //
          Class<?>[] parameterTypes = m.getParameterTypes();
          for (int i = 0;i < parameterTypes.length;i++) {
            Class<?> parameterType = parameterTypes[i];
            if (CommandContext.class.isAssignableFrom(parameterType)) {
              Type contextGenericParameterType = m.getGenericParameterTypes()[i];
              consumedType = (Class)TypeResolver.resolve(contextGenericParameterType, CommandContext.class, 0);
              producedType = (Class)TypeResolver.resolve(contextGenericParameterType, CommandContext.class, 1);
            }
          }
        }

        public void usage(ShellPrinter printer) {
          match.getDescriptor().printUsage(printer);
        }

        public void execute(
          CommandContext commandContext,
          String... args) throws ScriptException {

          //
          CRaSHCommand.this.context = commandContext;

          try {
            InvocationContext invocationContext = new InvocationContext();
            invocationContext.setAttribute(CommandContext.class, commandContext);
            Object o = match.invoke(invocationContext, CRaSHCommand.this);

            //
            if (o != null) {
              commandContext.getWriter().print(o);
            }
          } catch (Exception e) {
            e.printStackTrace();
            throw new ScriptException(e.getMessage(), e);
          } finally {
            CRaSHCommand.this.context = null;
          }
        }

        public Class getProducedType() {
          return producedType;
        }

        public Class getConsumedType() {
          return consumedType;
        }
      };
    } else {
      return null;
    }
  }
}
