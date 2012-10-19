/*
 * Copyright (C) 2012 eXo Platform SAS.
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
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.matcher.*;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.text.RenderPrintWriter;
import org.crsh.util.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class CRaSHCommand extends GroovyCommand implements ShellCommand {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private boolean unquoteArguments;

  /** . */
  private final ClassDescriptor<?> descriptor;

  /** The unmatched text, only valid during an invocation. */
  protected String unmatched;

  /** . */
  @Option(names = {"h","help"})
  @Usage("command usage")
  @Man("Provides command usage")
  private boolean help;

  protected CRaSHCommand() throws IntrospectionException {
    this.unquoteArguments = true;
    this.descriptor = CommandFactory.create(getClass());
    this.help = false;
    this.unmatched = null;
  }

  /**
   * Returns the command descriptor.
   *
   * @return the command descriptor
   */
  public ClassDescriptor<?> getDescriptor() {
    return descriptor;
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

  public final String getUnmatched() {
    return unmatched;
  }

  public final CommandCompletion complete(CommandContext context, String line) {

    // WTF
    Matcher analyzer = descriptor.matcher("main");

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
      return new CommandCompletion(Delimiter.EMPTY, ValueCompletion.create());
    } finally {
      this.context = null;
    }
  }

  public final String describe(String line, DescriptionFormat mode) {

    // WTF
    Matcher analyzer = descriptor.matcher("main");

    //
    CommandMatch match = analyzer.match(line);

    //
    try {
      switch (mode) {
        case DESCRIBE:
          return match.getDescriptor().getUsage();
        case MAN:
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          match.printMan(pw);
          return sw.toString();
        case USAGE:
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2 = new PrintWriter(sw2);
          match.printUsage(pw2);
          return sw2.toString();
      }
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }

    //
    return null;
  }

  static ScriptException toScript(Throwable cause) {
    if (cause instanceof ScriptException) {
      return (ScriptException)cause;
    } if (cause instanceof groovy.util.ScriptException) {
      // Special handling for groovy.util.ScriptException
      // which may be thrown by scripts because it is imported by default
      // by groovy imports
      String msg = cause.getMessage();
      ScriptException translated;
      if (msg != null) {
        translated = new ScriptException(msg);
      } else {
        translated = new ScriptException();
      }
      translated.setStackTrace(cause.getStackTrace());
      return translated;
    } else {
      return new ScriptException(cause);
    }
  }

  public CommandInvoker<?, ?> resolveInvoker(String name, Map<String, ?> options, List<?> args) {
    if (options.containsKey("h") || options.containsKey("help")) {
      throw new UnsupportedOperationException("Implement me");
    } else {

      Matcher matcher = descriptor.matcher("main");
      CommandMatch<CRaSHCommand, ?, ?> match = matcher.match(name, options, args);
      return resolveInvoker(match);
    }
  }

  public CommandInvoker<?, ?> resolveInvoker(String line) {
    Matcher analyzer = descriptor.matcher("main");
    final CommandMatch<CRaSHCommand, ?, ?> match = analyzer.match(line);
    return resolveInvoker(match);
  }

  public final CommandInvoker<?, ?> resolveInvoker(final CommandMatch<CRaSHCommand, ?, ?> match) {
    if (match instanceof MethodMatch) {

      //
      final MethodMatch<CRaSHCommand> methodMatch = (MethodMatch<CRaSHCommand>)match;

      //
      boolean help = false;
      for (OptionMatch optionMatch : methodMatch.getOwner().getOptionMatches()) {
        ParameterDescriptor<?> parameterDesc = optionMatch.getParameter();
        if (parameterDesc instanceof OptionDescriptor<?>) {
          OptionDescriptor<?> optionDesc = (OptionDescriptor<?>)parameterDesc;
          if (optionDesc.getNames().contains("h")) {
            help = true;
          }
        }
      }
      final boolean doHelp = help;

      //
      return new CommandInvoker() {

        Class consumedType = Void.class;
        Class producedType = Object.class;

        {
          // Try to find a command context argument
          Method m = methodMatch.getDescriptor().getMethod();

          //
          Class<?>[] parameterTypes = m.getParameterTypes();
          for (int i = 0;i < parameterTypes.length;i++) {
            Class<?> parameterType = parameterTypes[i];
            if (InvocationContext.class.isAssignableFrom(parameterType)) {
              Type contextGenericParameterType = m.getGenericParameterTypes()[i];
              producedType = TypeResolver.resolveToClass(contextGenericParameterType, InvocationContext.class, 0);
              break;
            }
          }

          //
          if (PipeCommand.class.isAssignableFrom(m.getReturnType())) {
            Type ret = m.getGenericReturnType();
            consumedType = TypeResolver.resolveToClass(ret, PipeCommand.class, 0);
          }
        }

        public PipeCommand invoke(final InvocationContext context) throws ScriptException {
          if (doHelp) {
            try {
              match.printUsage(context.getWriter());
              return new PipeCommand() {
                public void provide(Object element) throws IOException {
                }
              };
            }
            catch (IOException e) {
              throw new AssertionError(e);
            }
          } else {

            //
            pushContext(context);

            //
            CRaSHCommand.this.unmatched = methodMatch.getRest();

            //
            final Resolver resolver = new Resolver() {
              public <T> T resolve(Class<T> type) {
                if (type.equals(InvocationContext.class)) {
                  return type.cast(context);
                } else {
                  return null;
                }
              }
            };

            //
            if (consumedType == Void.class) {
              return new PipeCommand() {

                @Override
                public void open() throws ScriptException {
                  Object o;
                  try {
                    o = methodMatch.invoke(resolver, CRaSHCommand.this);
                  } catch (CmdSyntaxException e) {
                    throw new SyntaxException(e.getMessage());
                  } catch (CmdInvocationException e) {
                    throw toScript(e.getCause());
                  } finally {
                    CRaSHCommand.this.context = null;
                    CRaSHCommand.this.unmatched = null;
                  }
                  if (o != null) {
                    context.getWriter().print(o);
                  }
                }

                @Override
                public void provide(Object element) throws ScriptException, IOException {
                  // We just drop the elements
                }

                @Override
                public void flush() throws IOException {
                  context.flush();
                }

                @Override
                public void close() throws ScriptException {
                  popContext();
                }
              };
            } else {

              // JULIEN : WE SHOULD SOMEHOW HONNOR THE FINALLY CLAUSE LIKE IN THE IF BLOCK

              try {
                return (PipeCommand)methodMatch.invoke(resolver, CRaSHCommand.this);
              } catch (CmdSyntaxException e) {
                throw new SyntaxException(e.getMessage());
              } catch (CmdInvocationException e) {
                throw toScript(e.getCause());
              }
            }
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
      final ClassMatch<?> classMatch = (ClassMatch)match;

      //
      boolean help = false;
      for (OptionMatch optionMatch : classMatch.getOptionMatches()) {
        ParameterDescriptor<?> parameterDesc = optionMatch.getParameter();
        if (parameterDesc instanceof OptionDescriptor<?>) {
          OptionDescriptor<?> optionDesc = (OptionDescriptor<?>)parameterDesc;
          if (optionDesc.getNames().contains("h")) {
            help = true;
          }
        }
      }
      final boolean doHelp = help;

      //
      return new CommandInvoker<Void, Object>() {
        public PipeCommand<Void> invoke(final InvocationContext<Object> context) throws ScriptException {
          try {
            if (doHelp) {
              match.printUsage(context.getWriter());
            } else {
              classMatch.printUsage(context.getWriter());
            }
            return new PipeCommand<Void>() {
              public void provide(Void element) throws IOException {
              }
              @Override
              public void flush() throws IOException {
                context.flush();
              }
            };
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }
        }

        public Class<Object> getProducedType() {
          return Object.class;
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
