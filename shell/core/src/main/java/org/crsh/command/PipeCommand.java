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

/**
 * A pipe command.
 *
 * @param <C> the consumed generic type
 * @param <P> the produced generic type
 */
public abstract class PipeCommand<C, P> implements CommandInvoker<C, P> {

  /** . */
  protected InvocationContext<P> context;

  /** . */
  protected boolean piped;

  public final Class<P> getProducedType() {
    throw new UnsupportedOperationException();
  }

  public final Class<C> getConsumedType() {
    throw new UnsupportedOperationException();
  }

  public final boolean isPiped() {
    return piped;
  }

  public void setPiped(boolean piped) {
    this.piped = piped;
  }

  /**
   * Open pipe.
   */
  public final void open(InvocationContext<P> context) throws ScriptException {
    this.context = context;

    //
    open();
  }

  /**
   * Open pipe.
   */
  public void open() throws ScriptException {
  }

  /**
   * Extends the throw clause of the {@link Pipe#provide(Object)} method.
   *
   * @param element the provided element
   * @throws ScriptException any script exception
   * @throws IOException any io exception
   */
  public void provide(C element) throws ScriptException, IOException {
  }

  /**
   * Flush pipe.
   *
   * @throws ScriptException any script exception
   * @throws IOException any io exception
   */
  public void flush() throws ScriptException, IOException {
  }

  /**
   * Close pipe.
   *
   * @throws ScriptException any script exception
   */
  public void close() throws ScriptException {
  }
}
