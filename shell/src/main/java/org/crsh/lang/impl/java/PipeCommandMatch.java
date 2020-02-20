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

import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.command.*;
import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.ErrorKind;
import org.crsh.text.ScreenContext;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.util.Utils;

import java.io.IOException;
import java.lang.reflect.Type;

/**
* @author Julien Viet
*/
class PipeCommandMatch<T extends BaseCommand, C, P, PC extends Pipe<C, P>> extends BaseCommandMatch<T, C, P> {

  /** . */
  final Type ret;

  /** . */
  final Class<C> consumedType;

  /** . */
  final Class<P> producedType;

  /** . */
  private final CommandInvoker<Instance<T>, PC> invoker;

  /** . */
  private final String name;

  public PipeCommandMatch(ClassShellCommand<T> baseShellCommand, CommandInvoker<Instance<T>, PC> invoker) {
    super(baseShellCommand);
    this.invoker = invoker;
    ret = invoker.getGenericReturnType();
    consumedType = (Class<C>)Utils.resolveToClass(ret, Pipe.class, 0);
    producedType = (Class<P>)Utils.resolveToClass(ret, Pipe.class, 1);
    name = baseShellCommand.getDescriptor().getName();
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
  BaseInvoker getInvoker(T command) throws CommandException {

    //
    return new BaseInvoker(command) {

      Pipe<C, P> real;
      InvocationContext<P> invocationContext;

      public Class<P> getProducedType() {
        return producedType;
      }

      public Class<C> getConsumedType() {
        return consumedType;
      }

      public void open(CommandContext<? super P> consumer) throws CommandException {
        // Java is fine with that but not intellij....
        CommandContext<P> consumer2 = (CommandContext<P>)consumer;
        open2(consumer2);
      }

      @Override
      public ScreenContext getScreenContext() {
        return real instanceof ScreenContext ? (ScreenContext)real : null;
      }

      @Override
      public KeyHandler getKeyHandler() {
        return real instanceof KeyHandler ? (KeyHandler)real : null;
      }

      public void open2(final CommandContext<P> consumer) throws CommandException {

        //
        invocationContext = new InvocationContextImpl<P>(consumer, ShellSafetyFactory.getCurrentThreadShellSafety());

        // Push context
        command.pushContext(invocationContext);

        //  Set the unmatched part
        command.unmatched = invoker.getMatch().getRest();

        //
        PC ret;
        try {
          ret = invoker.invoke(this);
        }
        catch (org.crsh.cli.impl.SyntaxException e) {
          throw new CommandException(ErrorKind.SYNTAX, "Syntax exception when executing command " + name, e);
        } catch (InvocationException e) {
          throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e.getCause());
        }

        // It's a pipe command
        if (ret != null) {
          real = ret;
          try {
            real.open(invocationContext);
          }
          catch (Exception e) {
            throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e);
          }
        }
      }

      public void provide(C element) throws IOException, CommandException {
        if (real != null) {
          try {
            real.provide(element);
          }
          catch (Exception e) {
            throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e);
          }
        }
      }

      public void flush() throws IOException {
        if (real != null) {
          real.flush();
        } else {
          invocationContext.flush();
        }
      }

      public void close() throws IOException, CommandException {
        try {
          try {
            if (real != null) {
              real.close();
            }
          }
          catch (Exception e) {
            throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e);
          } finally {
            try {
              invocationContext.close();
            }
            catch (Exception e) {
              throw new CommandException(ErrorKind.EVALUATION, e);
            }
          }
        } finally {
          command.popContext();
          command.unmatched = null;
        }
      }
    };
  }
}
