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
package org.crsh.lang.impl.java;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.ObjectCommandInvoker;
import org.crsh.cli.spi.Completer;
import org.crsh.command.*;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandMatch;
import org.crsh.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/** @author Julien Viet */
public class ClassShellCommand<T extends BaseCommand> extends Command<Instance<T>> {

  /** . */
  private final Class<T> clazz;

  /** . */
  private final CommandDescriptor<Instance<T>> descriptor;
  private final ShellSafety shellSafety;

  public ClassShellCommand(Class<T> clazz, ShellSafety shellSafety) throws IntrospectionException {
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());
    this.clazz = clazz;
    this.shellSafety = shellSafety;
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
  }

  public CommandDescriptor<Instance<T>> getDescriptor() {
    return descriptor;
  }

  protected Completer getCompleter(final RuntimeContext context) throws CommandException {
    final T command = createCommand();
    if (command instanceof Completer) {
      command.context = context;
      return (Completer)command;
    } else {
      return null;
    }
  }

  @Override
  protected CommandMatch<?, ?> resolve(InvocationMatch<Instance<T>> match) {

    // Cast to the object invoker
    org.crsh.cli.impl.invocation.CommandInvoker<Instance<T>,?> invoker = match.getInvoker();

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

  T createCommand() throws CommandException {
    T command;
    try {
      command = clazz.getConstructor().newInstance();
    }
    catch (InvocationTargetException e) {
      String name = clazz.getSimpleName();
      throw new CommandException(ErrorKind.EVALUATION, "Could not create command " + name + " instance", e.getCause());
    }
    catch (Exception e) {
      String name = clazz.getSimpleName();
      throw new CommandException(ErrorKind.INTERNAL, "Could not create command " + name + " instance", e);
    }
    return command;
  }

  private <C, P, PC extends Pipe<C, P>> CommandMatch<C, P> getPipeInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<Instance<T>, PC> invoker) {
    return new PipeCommandMatch<T, C, P, PC>(this, invoker);
  }

  private <P> CommandMatch<Void, P> getProducerInvoker(final org.crsh.cli.impl.invocation.CommandInvoker<Instance<T>, ?> invoker, final Class<P> producedType) {
    return new ProducerCommandMatch<T, P>(this, invoker, producedType, shellSafety);
  }

}
