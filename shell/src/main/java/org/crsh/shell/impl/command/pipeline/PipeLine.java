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

package org.crsh.shell.impl.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.keyboard.KeyHandler;

import java.io.IOException;

public class  PipeLine extends CommandInvoker<Void, Object> {

  /** . */
  private final CommandInvoker[] invokers;

  /** . */
  private CommandContext<?> current;

  public PipeLine(CommandInvoker[] invokers) {
    this.invokers = invokers;
    this.current = null;
  }

  public Class<Void> getConsumedType() {
    return Void.class;
  }

  public Class<Object> getProducedType() {
    return Object.class;
  }

  public void open(CommandContext<? super Object> consumer) throws IOException, CommandException {
    open(0, consumer);
  }

  private CommandContext open(final int index, final CommandContext last) throws IOException, CommandException {
    if (index < invokers.length) {

      //
      final CommandInvoker invoker = invokers[index];
      CommandContext next = open(index + 1, last);

      //
      Class produced = invoker.getProducedType();
      Class<?> consumed = invoker.getConsumedType();
      CommandInvokerAdapter filterContext = new CommandInvokerAdapter(invoker, consumed, produced);
      try {
        filterContext.open(next);
      }
      catch (Exception e) {
        throw new CommandException(ErrorKind.EVALUATION, e);
      }

      // Save current filter in field
      // so if anything wrong happens it will be closed
      current = filterContext;

      //
      return filterContext;
    } else {
      current = last;
      return last;
    }
  }

  @Override
  public KeyHandler getKeyHandler() {
    for (CommandInvoker<?, ?> invoker : invokers) {
      KeyHandler handler = invoker.getKeyHandler();
      if (handler != null) {
        return handler;
      }
    }
    return null;
  }

  public void provide(Void element) throws IOException {
    // Ignore
  }

  public void flush() throws IOException {
    current.flush();
  }

  public void close() throws IOException, CommandException {
    try {
      current.close();
    }
    catch (Exception e) {
      throw new CommandException(ErrorKind.EVALUATION, e);
    }
  }
}
