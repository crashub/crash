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

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionException;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.completion.CompletionMatcher;
import org.crsh.cli.impl.descriptor.CommandDescriptorImpl;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.invocation.Resolver;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.util.TypeResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseCommand extends AbstractCommand implements ShellCommand {

  /** . */
  private final Logger log = Logger.getLogger(getClass().getName());

  /** . */
  private final CommandDescriptorImpl<? extends BaseCommand> descriptor;

  /** The unmatched text, only valid during an invocation. */
  protected String unmatched;

  /** The resolver. */
  private final Resolver resolver = new Resolver() {
    public <T> T resolve(Class<T> type) {
      if (type.equals(InvocationContext.class)) {
        return type.cast(peekContext());
      } else {
        return null;
      }
    }
  };

  protected BaseCommand() throws IntrospectionException {

    //
    Class<? extends BaseCommand> clazz = getClass();
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());

    //
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
    this.unmatched = null;
  }

  /**
   * Returns the command descriptor.
   *
   * @return the command descriptor
   */
  public CommandDescriptor<? extends BaseCommand> getDescriptor() {
    return descriptor;
  }

  protected final String readLine(String msg) {
    return readLine(msg, true);
  }

  protected final String readLine(String msg, boolean echo) {
    if (context instanceof InvocationContext) {
      return ((InvocationContext)context).readLine(msg, echo);
    } else {
      throw new IllegalStateException("Cannot invoke read line without an invocation context");
    }
  }

  public final String getUnmatched() {
    return unmatched;
  }

  public final CompletionMatch complete(RuntimeContext context, String line) {

    // WTF
    CompletionMatcher analyzer = descriptor.completer("main");

    //
    Completer completer = this instanceof Completer ? (Completer)this : null;

    //
    this.context = context;
    try {
      return analyzer.match(completer, line);
    }
    catch (CompletionException e) {
      log.log(Level.SEVERE, "Error during completion of line " + line, e);
      return new CompletionMatch(Delimiter.EMPTY, Completion.create());
    }
    finally {
      this.context = null;
    }
  }

  public final String describe(String line, DescriptionFormat mode) {

    // WTF
    InvocationMatcher analyzer = descriptor.invoker("main");

    //
    InvocationMatch match;
    try {
      match = analyzer.match(line);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }

    //
    try {
      switch (mode) {
        case DESCRIBE:
          return match.getDescriptor().getUsage();
        case MAN:
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          match.getDescriptor().printMan(pw);
          return sw.toString();
        case USAGE:
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2 = new PrintWriter(sw2);
          match.getDescriptor().printUsage(pw2);
          return sw2.toString();
      }
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }

    //
    return null;
  }

  public CommandInvoker<?, ?> resolveInvoker(String name, Map<String, ?> options, List<?> args) {
    InvocationMatcher<? extends BaseCommand> matcher = descriptor.invoker("main");
    InvocationMatch<? extends BaseCommand> match;
    try {
      match = matcher.match(name, options, args);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }
    return resolveInvoker(match);
  }

  public CommandInvoker<?, ?> resolveInvoker(String line) {
    InvocationMatcher<? extends BaseCommand> analyzer = descriptor.invoker("main");
    InvocationMatch<? extends BaseCommand> match;
    try {
      match = analyzer.match(line);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }
    return resolveInvoker(match);
  }

  public final void execute(String s) throws ScriptException, IOException {
    InvocationContext<?> context = peekContext();
    CommandInvoker invoker = context.resolve(s);
    invoker.open(context);
    invoker.flush();
    invoker.close();
  }

  public final CommandInvoker<?, ?> resolveInvoker(final InvocationMatch<? extends BaseCommand> match) {
    return resolveInvoker2(match);
  }

  private <B extends BaseCommand> CommandInvoker<?, ?> resolveInvoker2(final InvocationMatch<B> match) {

    // Invoker
    org.crsh.cli.impl.invocation.CommandInvoker<B, ?> invoker = match.getInvoker();

    // Necessary...
    B _this = (B)this;

    // Do we have a pipe command or not ?
    if (PipeCommand.class.isAssignableFrom(invoker.getReturnType())) {
      org.crsh.cli.impl.invocation.CommandInvoker invoker2 = invoker;
      return getPipeCommandInvoker(invoker2, _this);
    } else {

      // A priori it could be any class
      Class<?> producedType = Object.class;

      // Override produced type from InvocationContext<P> if any
      Class<?>[] parameterTypes = invoker.getParameterTypes();
      for (int i = 0;i < parameterTypes.length;i++) {
        Class<?> parameterType = parameterTypes[i];
        if (InvocationContext.class.isAssignableFrom(parameterType)) {
          Type contextGenericParameterType = invoker.getGenericParameterTypes()[i];
          producedType = TypeResolver.resolveToClass(contextGenericParameterType, InvocationContext.class, 0);
          break;
        }
      }

      //
      return getInvoker(invoker, _this, producedType);
    }
  }

  private <C, P, B extends BaseCommand, PC extends PipeCommand<C, P>> CommandInvoker<C, P> getPipeCommandInvoker(
      final org.crsh.cli.impl.invocation.CommandInvoker<B, PC> invoker, final B instance) {
    return new CommandInvoker<C, P>() {

      /** . */
      final Type ret = invoker.getGenericReturnType();

      /** . */
      final Class<C> consumedType = (Class<C>)TypeResolver.resolveToClass(ret, PipeCommand.class, 0);

      /** . */
      final Class<P> producedType = (Class<P>)TypeResolver.resolveToClass(ret, PipeCommand.class, 1);

      PipeCommand<C, P> real;

      public Class<P> getProducedType() {
        return producedType;
      }

      public Class<C> getConsumedType() {
        return consumedType;
      }

      public void open(CommandContext<? super P> consumer) {
        // Java is fine with that but not intellij....
        CommandContext<P> consumer2 = (CommandContext<P>)consumer;
        open2(consumer2);
      }

      public void open2(final CommandContext<P> consumer) {

        //
        final InvocationContextImpl<P> invocationContext = new InvocationContextImpl<P>(consumer);

        // Push context
        pushContext(invocationContext);

        //  Set the unmatched part
        BaseCommand.this.unmatched = invoker.getMatch().getRest();

        //
        PC ret;
        try {
          ret = invoker.invoke(resolver, instance);
        }
        catch (org.crsh.cli.SyntaxException e) {
          throw new SyntaxException(e.getMessage());
        } catch (InvocationException e) {
          throw toScript(e.getCause());
        }

        // It's a pipe command
        if (ret != null) {
          real = ret;
          real.doOpen(invocationContext);
        }
      }

      public void provide(C element) throws IOException {
        if (real != null) {
          real.provide(element);
        }
      }

      public void flush() throws IOException {
        if (real != null) {
          real.flush();
        } else {
          peekContext().flush();
        }
      }

      public void close() throws IOException {
        if (real != null) {
          try {
            real.close();
          }
          finally {
            popContext();
          }
        } else {
          InvocationContext<?> context = popContext();
          context.close();
        }
        BaseCommand.this.unmatched = null;
      }
    };
  }

  private <P, B extends BaseCommand> CommandInvoker<Void, P> getInvoker(
      final org.crsh.cli.impl.invocation.CommandInvoker<B, ?> invoker,
      final B instance,
      final Class<P> _producedType) {
    return new CommandInvoker<Void, P>() {

      public Class<P> getProducedType() {
        return _producedType;
      }

      public Class<Void> getConsumedType() {
        return Void.class;
      }

      public void open(CommandContext<? super P> consumer) {
        // Java is fine with that but not intellij....
        CommandContext<P> consumer2 = (CommandContext<P>)consumer;
        open2(consumer2);
      }

      public void open2(final CommandContext<P> consumer) {

        //
        final InvocationContextImpl<P> invocationContext = new InvocationContextImpl<P>(consumer);

        // Push context
        pushContext(invocationContext);

        //  Set the unmatched part
        BaseCommand.this.unmatched = invoker.getMatch().getRest();
      }

      public void provide(Void element) throws IOException {
        // Drop everything
      }
      public void flush() throws IOException {
        // peekContext().flush();
      }
      public void close() throws IOException, UndeclaredThrowableException {

        //
        Object ret;
        try {
          ret = invoker.invoke(resolver, instance);
        }
        catch (org.crsh.cli.SyntaxException e) {
          throw new SyntaxException(e.getMessage());
        } catch (InvocationException e) {
          throw toScript(e.getCause());
        }

        //
        if (ret != null) {
          peekContext().getWriter().print(ret);
        }

        //
        InvocationContext<?> context = popContext();
        context.flush();
        context.close();
        BaseCommand.this.unmatched = null;
      }
    };
  }

  public UndeclaredThrowableException toScript(Throwable cause) {
    return new UndeclaredThrowableException(cause);
  }
}
