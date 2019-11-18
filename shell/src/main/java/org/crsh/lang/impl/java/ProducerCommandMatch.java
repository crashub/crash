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
import org.crsh.command.BaseCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.InvocationContext;
import org.crsh.command.ShellSafety;
import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.shell.impl.command.spi.CommandException;

import java.io.IOException;

/**
* @author Julien Viet
*/
class ProducerCommandMatch<T extends BaseCommand, P> extends BaseCommandMatch<T, Void, P> {

  /** . */
  private final CommandInvoker<Instance<T>, ?> invoker;

  /** . */
  private final Class<P> producedType;

  /** . */
  private final String name;
  private ShellSafety shellSafety;

  public ProducerCommandMatch(ClassShellCommand<T> shellCommand, CommandInvoker<Instance<T>, ?> invoker, Class<P> producedType, ShellSafety shellSafety) {
    super(shellCommand);

    //
    this.invoker = invoker;
    this.producedType = producedType;
    this.name = shellCommand.getDescriptor().getName();
    this.shellSafety = shellSafety;
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
  BaseInvoker getInvoker(T command) throws CommandException {

    //
    return new BaseInvoker(command) {

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
        invocationContext = new InvocationContextImpl<P>(consumer, shellSafety);
        command.pushContext(invocationContext);
        command.unmatched = invoker.getMatch().getRest();
      }


      @Override
      public KeyHandler getKeyHandler() {
        if (command instanceof KeyHandler) {
          return (KeyHandler)command;
        } else {
          return null;
        }
      }

      public void provide(Void element) {
        // Drop everything
      }

      public void flush() {
      }

      public void close() throws IOException, CommandException {
        try {
          Object ret;
          try {
            ret = invoker.invoke(this);
          }
          catch (org.crsh.cli.impl.SyntaxException e) {
            throw new CommandException(ErrorKind.SYNTAX, "Syntax exception when executing command " + name, e);
          } catch (InvocationException e) {
            throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e.getCause());
          }

          // Anything returned compatible is then produced
          if (ret != null && producedType.isInstance(ret)) {
            P produced = producedType.cast(ret);
            try {
              invocationContext.provide(produced);
            }
            catch (Exception e) {
              throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e);
            }
          }
        } finally {
          try {
            invocationContext.flush();
          }
          finally {
            try {
              invocationContext.close();
            }
            catch (Exception e) {
              throw new CommandException(ErrorKind.EVALUATION, "Command " + name + " failed", e);
            } finally {
              command.unmatched = null;
              invocationContext = null;
            }
          }
        }
      }
    };
  }
}
