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

import org.crsh.command.CommandContext;
import org.crsh.keyboard.KeyHandler;
import org.crsh.stream.Filter;
import org.crsh.text.ScreenContext;

import java.io.IOException;

/**
 * A command invoker is a filter for a {@link org.crsh.command.CommandContext} kind of consumer.
 *
 * @param <C> the consumed element generic type
 * @param <P> the produced element generic type
 */
public abstract class CommandInvoker<C, P> implements Filter<C, P, CommandContext<? super P>> {

  /**
   * Invoke the command.
   *
   * @param consumer the consumer for this invocation
   * @throws IOException any io exception
   * @throws CommandException anything command exception
   */
  public final void invoke(CommandContext<? super P> consumer) throws IOException, CommandException {
    try {
      open(consumer);
    }
    finally {
      try {
        flush();
      }
      finally {
        close();
      }
    }
  }

  @Override
  public abstract void flush() throws IOException;

  @Override
  public abstract void provide(C element) throws IOException, CommandException;

  @Override
  public abstract void open(CommandContext<? super P> consumer) throws IOException, CommandException;

  @Override
  public abstract void close() throws IOException, CommandException;

  /**
   * Provide an opportunity for the command to implement screen context.
   *
   * @return the screen context
   */
  public ScreenContext getScreenContext() {
    return null;
  }

  /**
   * Return the key handler or null if the invoker cannot handler key events.
   *
   * @return the key handler
   */
  public KeyHandler getKeyHandler() {
    return null;
  }
}
