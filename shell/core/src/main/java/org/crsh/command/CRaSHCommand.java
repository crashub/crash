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
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.cmdline.matcher.ClassMatch;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.MethodMatch;
import org.crsh.cmdline.spi.Completer;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * A real CRaSH command, the most powerful kind of command.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CRaSHCommand extends GroovyCommand implements ShellCommand {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

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
    if (context instanceof InvocationContext) {
      return ((InvocationContext)context).readLine(msg, echo);
    } else {
      throw new IllegalStateException("No current context of interaction with the term");
    }
  }

  @Override
  protected final CommandContext getContext() {
    return context;
  }

  public Map<String, String> complete(CommandContext context, String line) {

    // WTF
    Matcher analyzer = new Matcher("main", descriptor);

    //
    Completer completer = this instanceof Completer ? (Completer)this : null;

    //
    try {
      this.context = context;

      //
      return analyzer.complete(completer, line);
    }
    catch (CmdCompletionException e) {
      log.error("Error during completion of line " + line, e);
      return Collections.emptyMap();
    } finally {
      this.context = null;
    }
  }

  public String describe(String line, DescriptionMode mode) {

    // WTF
    Matcher analyzer = new Matcher("main", descriptor);

    //
    CommandMatch match = analyzer.match("line");

    //
    switch (mode) {
      case DESCRIBE:
        return match.getDescriptor().getDescription();
      case MAN:
        if (match instanceof MethodMatch) {
          MethodMatch methodMatch = (MethodMatch)match;
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          methodMatch.getDescriptor().printUsage(pw);
          return sw.toString();
        } else {
          break;
        }
      case USAGE:
    }

    //
    return null;
  }

  public CommandInvoker<?, ?> createInvoker(String line) {

    // Remove surrounding quotes if there are
    if (unquoteArguments) {
      // todo ?
    }

    // WTF
    Matcher analyzer = new Matcher("main", descriptor);

    //
    final CommandMatch match = analyzer.match(line);

    //
    if (match instanceof MethodMatch) {

      //
      final MethodMatch methodMatch = (MethodMatch)match;

      //
      return new CommandInvoker() {

        Class consumedType = Void.class;
        Class producedType = Void.class;

        {
          // Try to find a command context argument
          Method m = methodMatch.getDescriptor().getMethod();

          //
          Class<?>[] parameterTypes = m.getParameterTypes();
          for (int i = 0;i < parameterTypes.length;i++) {
            Class<?> parameterType = parameterTypes[i];
            if (InvocationContext.class.isAssignableFrom(parameterType)) {
              Type contextGenericParameterType = m.getGenericParameterTypes()[i];
              consumedType = (Class)TypeResolver.resolve(contextGenericParameterType, InvocationContext.class, 0);
              producedType = (Class)TypeResolver.resolve(contextGenericParameterType, InvocationContext.class, 1);
            }
          }
        }

        public void usage(ShellPrinter printer) {
          methodMatch.getDescriptor().printUsage(printer);
        }

        public void invoke(InvocationContext context) throws ScriptException {

          //
          CRaSHCommand.this.context = context;

          try {
            org.crsh.cmdline.matcher.InvocationContext invocationContext = new org.crsh.cmdline.matcher.InvocationContext();
            invocationContext.setAttribute(InvocationContext.class, context);
            Object o = match.invoke(invocationContext, CRaSHCommand.this);

            //
            if (o != null) {
              context.getWriter().print(o);
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
    } else if (match instanceof ClassMatch) {

      //
      final ClassMatch classMatch = (ClassMatch)match;

      //
      return new CommandInvoker<Void, Void>() {
        public void usage(ShellPrinter printer) {
          classMatch.getDescriptor().printUsage(printer);
        }

        public void invoke(InvocationContext<Void, Void> context) throws ScriptException {
          classMatch.getDescriptor().printUsage(context.getWriter());
        }

        public Class<Void> getProducedType() {
          return Void.class;
        }

        public Class<Void> getConsumedType() {
          return Void.class;
        }
      };

    } else {
      return null;
    }
  }
}
