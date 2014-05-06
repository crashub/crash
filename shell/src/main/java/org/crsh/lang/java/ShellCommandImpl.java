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
package org.crsh.lang.java;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.ObjectCommandInvoker;
import org.crsh.cli.spi.Completer;
import org.crsh.command.BaseCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandCreationException;
import org.crsh.command.InvocationContext;
import org.crsh.shell.impl.command.spi.InvocationContextImpl;
import org.crsh.command.Pipe;
import org.crsh.command.RuntimeContext;
import org.crsh.command.SyntaxException;
import org.crsh.console.KeyHandler;
import org.crsh.shell.ErrorType;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.util.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;

/** @author Julien Viet */
public class ShellCommandImpl<T extends BaseCommand> extends ShellCommand<org.crsh.cli.impl.lang.InvocationContext<T>> {

  /** . */
  private final Class<T> clazz;

  /** . */
  private final CommandDescriptor<org.crsh.cli.impl.lang.InvocationContext<T>> descriptor;

  public ShellCommandImpl(Class<T> clazz) {
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());
    this.clazz = clazz;
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
  }

  public CommandDescriptor<org.crsh.cli.impl.lang.InvocationContext<T>> getDescriptor() {
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
  protected Command<?, ?> resolveCommand(InvocationMatch<org.crsh.cli.impl.lang.InvocationContext<T>> match) {

    // Cast to the object invoker
    org.crsh.cli.impl.invocation.CommandInvoker<org.crsh.cli.impl.lang.InvocationContext<T>,?> invoker = match.getInvoker();

    // Do we have a pipe command or not ?
    if (Pipe.class.isAssignableFrom(invoker.getReturnType())) {
      org.crsh.cli.impl.invocation.CommandInvoker tmp = invoker;
      return getPipeInvoker(tmp);
    } else {

      // Determine the produced type
      Class<?> producedType;
      if (void.class.equals(invoker.getReturnType())) {
        producedType = Object.class;
      } else {
        producedType = invoker.getReturnType();
      }

      // Override produced type from InvocationContext<P> if any
      if (invoker instanceof ObjectCommandInvoker) {
        ObjectCommandInvoker<T, ?> objectInvoker = (ObjectCommandInvoker<T, ?>)invoker;
        Class<?>[] parameterTypes = objectInvoker.getParameterTypes();
        for (int i = 0;i < parameterTypes.length;i++) {
          Class<?> parameterType = parameterTypes[i];
          if (InvocationContext.class.isAssignableFrom(parameterType)) {
            Type contextGenericParameterType = objectInvoker.getGenericParameterTypes()[i];
            producedType = Utils.resolveToClass(contextGenericParameterType, InvocationContext.class, 0);
            break;
          }
        }
      }

      //
      return getProducerInvoker(invoker, producedType);
    }
  }

  private abstract class Bilto<T extends org.crsh.command.BaseCommand, C, P> extends Command<C, P> {

    /** . */
    private ShellCommandImpl<T> baseShellCommand;

    public Bilto(ShellCommandImpl<T> baseShellCommand) {
      this.baseShellCommand = baseShellCommand;
    }

    public CommandInvoker<C, P> getInvoker() throws CommandCreationException {
      final T instance = baseShellCommand.createCommand();
      org.crsh.cli.impl.lang.InvocationContext<T> resolver = new org.crsh.cli.impl.lang.InvocationContext<T>() {
        @Override
        public T getInstance() {
          return instance;
        }
        public <T> T resolve(Class<T> type) {
          if (type.equals(InvocationContext.class)) {
            return type.cast(instance.peekContext());
          } else {
            return null;
          }
        }
      };
      return getInvoker(resolver);
    }

    abstract CommandInvoker<C, P> getInvoker(org.crsh.cli.impl.lang.InvocationContext<T> context) throws CommandCreationException;
  }

  T createCommand() throws CommandCreationException {
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

  private <C, P, PC extends Pipe<C, P>> Command<C, P> getPipeInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<org.crsh.cli.impl.lang.InvocationContext<T>, PC> invoker) {
    return new Bilto<T, C, P>(this) {

      /** . */
      final Type ret = invoker.getGenericReturnType();

      /** . */
      final Class<C> consumedType = (Class<C>)Utils.resolveToClass(ret, Pipe.class, 0);

      /** . */
      final Class<P> producedType = (Class<P>)Utils.resolveToClass(ret, Pipe.class, 1);

      @Override
      public InvocationMatch<?> getMatch() {
        return invoker.getMatch();
      }

      @Override
      public Class<P> getProducedType() {
        return producedType;
      }

      @Override
      public Class<C> getConsumedType() {
        return consumedType;
      }

      @Override
      CommandInvoker<C, P> getInvoker(final org.crsh.cli.impl.lang.InvocationContext<T> context) throws CommandCreationException {
        return new CommandInvoker<C, P>() {

          Pipe<C, P> real;
          InvocationContext<P> invocationContext;

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
            if (context.getInstance() instanceof KeyHandler) {
              return (KeyHandler)context.getInstance();
            } else {
              return null;
            }
          }

          public void open2(final CommandContext<P> consumer) {

            //
            invocationContext = new InvocationContextImpl<P>(consumer);

            // Push context
            context.getInstance().pushContext(invocationContext);

            //  Set the unmatched part
            context.getInstance().unmatched = invoker.getMatch().getRest();

            //
            PC ret;
            try {
              ret = invoker.invoke(context);
            }
            catch (org.crsh.cli.SyntaxException e) {
              throw new SyntaxException(e.getMessage());
            } catch (InvocationException e) {
              throw context.getInstance().toScript(e.getCause());
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
              invocationContext.flush();
            }
          }

          public void close() throws IOException {
            T instance = context.getInstance();
            try {
              if (real != null) {
                try {
                  real.close();
                }
                finally {
                  Utils.close(invocationContext);
                }
              } else {
                Utils.close(invocationContext);
              }
            }
            finally {
              instance.popContext();
              instance.unmatched = null;
            }
          }
        };
      }
    };
  }

  private <P> Command<Void, P> getProducerInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<org.crsh.cli.impl.lang.InvocationContext<T>, ?> invoker, final Class<P> producedType) {
    return new Bilto<T, Void, P>(this) {

      @Override
      public InvocationMatch<?> getMatch() {
        return invoker.getMatch();
      }

      @Override
      public Class<P> getProducedType() {
        return producedType;
      }

      @Override
      public Class<Void> getConsumedType() {
        return Void.class;
      }

      @Override
      CommandInvoker<Void, P> getInvoker(final org.crsh.cli.impl.lang.InvocationContext<T> context) throws CommandCreationException {
        return new CommandInvoker<Void, P>() {

          /** . */
          private InvocationContext<P> invocationContext;

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
            invocationContext = new InvocationContextImpl<P>(consumer);

            // Push context
            context.getInstance().pushContext(invocationContext);

            //  Set the unmatched part
            context.getInstance().unmatched = invoker.getMatch().getRest();
          }


          @Override
          public KeyHandler getKeyHandler() {
            if (context.getInstance() instanceof KeyHandler) {
              return (KeyHandler)context.getInstance();
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
              ret = invoker.invoke(context);
            }
            catch (org.crsh.cli.SyntaxException e) {
              throw new SyntaxException(e.getMessage());
            } catch (InvocationException e) {
              throw context.getInstance().toScript(e.getCause());
            }

            //
            if (ret != null && producedType.isInstance(ret)) {
              P produced = producedType.cast(ret);
              invocationContext.provide(produced);
            }

            //
            invocationContext.flush();
            invocationContext.close();
            context.getInstance().unmatched = null;
            invocationContext = null;
          }
        };
      }
    };
  }
}
