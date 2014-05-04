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
package org.crsh.shell.impl.command.spi;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.Resolver;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Util;
import org.crsh.cli.spi.Completer;
import org.crsh.command.BaseCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandCreationException;
import org.crsh.command.InvocationContext;
import org.crsh.command.InvocationContextImpl;
import org.crsh.command.PipeCommand;
import org.crsh.command.RuntimeContext;
import org.crsh.command.SyntaxException;
import org.crsh.console.KeyHandler;
import org.crsh.shell.ErrorType;
import org.crsh.util.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;

/** @author Julien Viet */
public class BaseShellCommand<T extends BaseCommand> extends ShellCommand<T> {

  /** . */
  private final Class<T> clazz;

  /** . */
  private final CommandDescriptor<T> descriptor;

  public BaseShellCommand(Class<T> clazz) {
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());
    this.clazz = clazz;
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
  }

  public CommandDescriptor<T> getDescriptor() {
    return descriptor;
  }

  protected Completer getCompleter(final RuntimeContext context) throws CommandCreationException {
    final T command = createCommand();
    if (command instanceof Completer) {
      command.context = context;
      return (Completer)command;
    } else {
      return null;
    }
  }

  @Override
  public String describe(final InvocationMatch<T> match, DescriptionFormat mode) {
    final Bilto<?, ?> bilto = resolveInvoker2(match);

    //
    try {
      switch (mode) {
        case DESCRIBE:
          return match.getDescriptor().getUsage();
        case MAN: {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          Format.Man man = new Format.Man() {
            @Override
            public void printSynopsisSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
              super.printSynopsisSection(command, stream);

              // Extra stream section
              if (match.getDescriptor().getSubordinates().isEmpty()) {
                stream.append("STREAM\n");
                stream.append(Util.MAN_TAB);
                printFQN(command, stream);
                stream.append(" <").append(bilto.getConsumedType().getName()).append(", ").append(bilto.getProducedType().getName()).append('>');
                stream.append("\n\n");
              }
            }
          };
          match.getDescriptor().print(man, pw);
          return sw.toString();
        }
        case USAGE: {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          match.getDescriptor().printUsage(pw);
          return sw.toString();
        }
      }
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }

    //
    return null;
  }

  // Need to find a decent name...
  private abstract class Bilto<C, P> {

    CommandInvoker<C, P> getInvoker() throws CommandCreationException {
      final T instance = createCommand();
      Resolver resolver = new Resolver() {
        public <T> T resolve(Class<T> type) {
          if (type.equals(InvocationContext.class)) {
            return type.cast(instance.peekContext());
          } else {
            return null;
          }
        }
      };
      return getInvoker(instance, resolver);
    }

    abstract InvocationMatch<?> getMatch();

    abstract CommandInvoker<C, P> getInvoker(T instance, Resolver resolver) throws CommandCreationException;

    abstract Class<P> getProducedType();

    abstract Class<C> getConsumedType();
  }

  public CommandInvoker<?, ?> resolveInvoker(InvocationMatch<T> match) throws CommandCreationException {
    // Please remove me
    return resolveInvoker2((InvocationMatch<T>)match).getInvoker();
  }


  private Bilto<?, ?> resolveInvoker2(final InvocationMatch<T> match) {

    // Invoker
    org.crsh.cli.impl.invocation.CommandInvoker<T, ?> invoker = match.getInvoker();

    // Do we have a pipe command or not ?
    if (PipeCommand.class.isAssignableFrom(invoker.getReturnType())) {
      org.crsh.cli.impl.invocation.CommandInvoker invoker2 = invoker;
      return getPipeCommandInvoker(invoker2);
    } else {

      // A priori it could be any class
      Class<?> producedType = Object.class;

      // Override produced type from InvocationContext<P> if any
      Class<?>[] parameterTypes = invoker.getParameterTypes();
      for (int i = 0;i < parameterTypes.length;i++) {
        Class<?> parameterType = parameterTypes[i];
        if (InvocationContext.class.isAssignableFrom(parameterType)) {
          Type contextGenericParameterType = invoker.getGenericParameterTypes()[i];
          producedType = Utils.resolveToClass(contextGenericParameterType, InvocationContext.class, 0);
          break;
        }
      }

      //
      return getInvoker(invoker, producedType);
    }
  }

  private T createCommand() throws CommandCreationException {
    T command;
    try {
      command = clazz.newInstance();
    }
    catch (Exception e) {
      String name = clazz.getSimpleName();
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not create command " + name + " instance", e);
    }
    return command;
  }

  private <C, P, PC extends PipeCommand<C, P>> Bilto<C, P> getPipeCommandInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<T, PC> invoker) {
    return new Bilto<C, P>() {

      /** . */
      final Type ret = invoker.getGenericReturnType();

      /** . */
      final Class<C> consumedType = (Class<C>)Utils.resolveToClass(ret, PipeCommand.class, 0);

      /** . */
      final Class<P> producedType = (Class<P>)Utils.resolveToClass(ret, PipeCommand.class, 1);

      @Override
      InvocationMatch<?> getMatch() {
        return invoker.getMatch();
      }

      @Override
      Class<P> getProducedType() {
        return producedType;
      }

      @Override
      Class<C> getConsumedType() {
        return consumedType;
      }

      @Override
      CommandInvoker<C, P> getInvoker(final T instance, final Resolver resolver) throws CommandCreationException {
        return new CommandInvoker<C, P>() {

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

          @Override
          public KeyHandler getKeyHandler() {
            if (instance instanceof KeyHandler) {
              return (KeyHandler)instance;
            } else {
              return null;
            }
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
    };
  }

  private <P> Bilto<Void, P> getInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<T, ?> invoker, final Class<P> producedType) {
    return new Bilto<Void, P>() {

      @Override
      InvocationMatch<?> getMatch() {
        return invoker.getMatch();
      }

      @Override
      Class<P> getProducedType() {
        return producedType;
      }

      @Override
      Class<Void> getConsumedType() {
        return Void.class;
      }

      @Override
      CommandInvoker<Void, P> getInvoker(final T instance, final Resolver resolver) throws CommandCreationException {
        return new CommandInvoker<Void, P>() {

          public Class<P> getProducedType() {
            return producedType;
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


          @Override
          public KeyHandler getKeyHandler() {
            if (instance instanceof KeyHandler) {
              return (KeyHandler)instance;
            } else {
              return null;
            }
          }

          public void provide(Void element) throws IOException {
            // Drop everything
          }

          public void flush() throws IOException {
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
    };
  }
}
