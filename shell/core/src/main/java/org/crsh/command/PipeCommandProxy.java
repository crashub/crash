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

import org.crsh.Pipe;

import java.io.IOException;

class PipeCommandProxy<E> extends PipeCommand<E> {

  /** . */
  private final PipeCommand<E> delegate;

  /** . */
  private final Pipe<E> next;

  PipeCommandProxy(PipeCommand<E> delegate, Pipe<E> next) {
    this.delegate = delegate;
    this.next = next;
  }

  @Override
  public void open() throws ScriptException {
    if (next != null && next instanceof PipeCommand) {
      ((PipeCommand)next).open();
    }
    delegate.setPiped(isPiped());
    delegate.open();
  }

  @Override
  public void provide(E element) throws ScriptException, IOException {
    delegate.provide(element);
  }

  @Override
  public void flush() throws ScriptException, IOException {
    delegate.flush();
  }

  @Override
  public void close() throws ScriptException {
    delegate.close();
    if (next != null && next instanceof PipeCommand) {
      ((PipeCommand)next).close();
    }
  }
}
