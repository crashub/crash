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
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.invocation.Resolver;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.ErrorType;
import org.crsh.util.TypeResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** @author Julien Viet */
public class BaseShellCommand<CC extends BaseCommand> implements ShellCommand {

  /** . */
  private final Class<CC> clazz;

  /** . */
  private final CommandDescriptor<CC> descriptor;

  public BaseShellCommand(Class<CC> clazz) {

    //
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());

    //
    this.clazz = clazz;
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
  }

  public CommandDescriptor<? extends BaseCommand> getDescriptor() {
    return descriptor;
  }

  public final CompletionMatch complete(RuntimeContext context, String line) throws CommandCreationException {

    // WTF
    CompletionMatcher analyzer = descriptor.completer("main");

    //
    CC command = createCommand();

    //
    Completer completer = command instanceof Completer ? (Completer)command : null;

    //
    command.context = context;
    try {
      return analyzer.match(completer, line);
    }
    catch (CompletionException e) {
      command.log.log(Level.SEVERE, "Error during completion of line " + line, e);
      return new CompletionMatch(Delimiter.EMPTY, Completion.create());
    }
    finally {
      command.context = null;
    }
  }

  public final String describe(String line, DescriptionFormat mode) {

    // WTF
    InvocationMatcher analyzer = descriptor.matcher("main");

    //
    InvocationMatch match;
    try {
      match = analyzer.parse(line);
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

  public CommandInvoker<?, ?> resolveInvoker(Map<String, ?> options, String subordinate, Map<String, ?> subordinateOptions, List<?> arguments) throws CommandCreationException {
    InvocationMatcher<CC> matcher = descriptor.matcher("main");

    //
    if (options != null && options.size() > 0) {
      for (Map.Entry<String, ?> option : options.entrySet()) {
        matcher = matcher.option(option.getKey(), Collections.singletonList(option.getValue()));
      }
    }

    //
    if (subordinate != null && subordinate.length() > 0) {
      matcher = matcher.subordinate(subordinate);

      // Minor : remove that and use same signature
      if (subordinateOptions != null && subordinateOptions.size() > 0) {
        for (Map.Entry<String, ?> option : subordinateOptions.entrySet()) {
          matcher = matcher.option(option.getKey(), Collections.singletonList(option.getValue()));
        }
      }
    }

    //
    InvocationMatch<CC> match = matcher.arguments(arguments != null ? arguments : Collections.emptyList());

    //
    return resolveInvoker(match);
  }

  public CommandInvoker<?, ?> resolveInvoker(String line) throws CommandCreationException {
    InvocationMatcher<CC> analyzer = descriptor.matcher("main");
    InvocationMatch<CC> match;
    try {
      match = analyzer.parse(line);
    }
    catch (org.crsh.cli.SyntaxException e) {
      throw new SyntaxException(e.getMessage());
    }
    return resolveInvoker(match);
  }

  public final CommandInvoker<?, ?> resolveInvoker(final InvocationMatch<CC> match) throws CommandCreationException {
    return resolveInvoker2(match);
  }

  private CommandInvoker<?, ?> resolveInvoker2(final InvocationMatch<CC> match) throws CommandCreationException {

    // Invoker
    org.crsh.cli.impl.invocation.CommandInvoker<CC, ?> invoker = match.getInvoker();

    // Necessary...
    final CC command = createCommand();

    /** The resolver. */
    Resolver resolver = new Resolver() {
      public <T> T resolve(Class<T> type) {
        if (type.equals(InvocationContext.class)) {
          return type.cast(command.peekContext());
        } else {
          return null;
        }
      }
    };

    // Do we have a pipe command or not ?
    if (PipeCommand.class.isAssignableFrom(invoker.getReturnType())) {
      org.crsh.cli.impl.invocation.CommandInvoker invoker2 = invoker;
      return getPipeCommandInvoker(invoker2, command, resolver);
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
      return getInvoker(invoker, command, producedType, resolver);
    }
  }

  private CC createCommand() throws CommandCreationException {
    CC command;
    try {
      command = clazz.newInstance();
    }
    catch (Exception e) {
      String name = clazz.getSimpleName();
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not create command " + name + " instance", e);
    }
    return command;
  }

  private <C, P, PC extends PipeCommand<C, P>> CommandInvoker<C, P> getPipeCommandInvoker(
      final org.crsh.cli.impl.invocation.CommandInvoker<CC, PC> invoker,
      final CC instance,
      final Resolver resolver) {
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
        instance.pushContext(invocationContext);

        //  Set the unmatched part
        instance.unmatched = invoker.getMatch().getRest();

        //
        PC ret;
        try {
          ret = invoker.invoke(resolver, instance);
        }
        catch (org.crsh.cli.SyntaxException e) {
          throw new SyntaxException(e.getMessage());
        } catch (InvocationException e) {
          throw instance.toScript(e.getCause());
        }

        // It's a pipe command
        if (ret != null) {
          real = ret;
          real.open(invocationContext);
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
          instance.peekContext().flush();
        }
      }

      public void close() throws IOException {
        if (real != null) {
          try {
            real.close();
          }
          finally {
            instance.popContext();
          }
        } else {
          InvocationContext<?> context = instance.popContext();
          context.close();
        }
        instance.unmatched = null;
      }
    };
  }

  private <P> CommandInvoker<Void, P> getInvoker(
      final org.crsh.cli.impl.invocation.CommandInvoker<CC, ?> invoker,
      final CC instance,
      final Class<P> _producedType,
      final Resolver resolver) {
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
        instance.pushContext(invocationContext);

        //  Set the unmatched part
        instance.unmatched = invoker.getMatch().getRest();
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
          throw instance.toScript(e.getCause());
        }

        //
        if (ret != null) {
          instance.peekContext().getWriter().print(ret);
        }

        //
        InvocationContext<?> context = instance.popContext();
        context.flush();
        context.close();
        instance.unmatched = null;
      }
    };
  }
}
