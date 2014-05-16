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

import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.command.BaseCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.command.SyntaxException;
import org.crsh.console.KeyHandler;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.util.Utils;

import java.io.IOException;
import java.lang.reflect.Type;

/**
* @author Julien Viet
*/
class PipeCommandImpl<T extends BaseCommand, C, P, PC extends Pipe<C, P>> extends CommandImpl<T, C, P> {

  /** . */
  final Type ret;

  /** . */
  final Class<C> consumedType;

  /** . */
  final Class<P> producedType;
  private final CommandInvoker<Instance<T>, PC> invoker;

  public PipeCommandImpl(ClassShellCommand<T> baseShellCommand, CommandInvoker<Instance<T>, PC> invoker) {
    super(baseShellCommand);
    this.invoker = invoker;
    ret = invoker.getGenericReturnType();
    consumedType = (Class<C>)Utils.resolveToClass(ret, Pipe.class, 0);
    producedType = (Class<P>)Utils.resolveToClass(ret, Pipe.class, 1);
  }

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
  BaseInvoker getInvoker(T command) throws CommandCreationException {

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

      public void open(CommandContext<? super P> consumer) {
        // Java is fine with that but not intellij....
        CommandContext<P> consumer2 = (CommandContext<P>)consumer;
        open2(consumer2);
      }

      @Override
      public KeyHandler getKeyHandler() {
        return command instanceof KeyHandler ? (KeyHandler)command : null;
      }

      public void open2(final CommandContext<P> consumer) {

        //
        invocationContext = new InvocationContextImpl<P>(consumer);

        // Push context
        command.pushContext(invocationContext);

        //  Set the unmatched part
        command.unmatched = invoker.getMatch().getRest();

        //
        PC ret;
        try {
          ret = invoker.invoke(this);
        }
        catch (org.crsh.cli.SyntaxException e) {
          throw new SyntaxException(e.getMessage());
        } catch (InvocationException e) {
          throw command.toScript(e.getCause());
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
          command.popContext();
          command.unmatched = null;
        }
      }
    };
  }
}
