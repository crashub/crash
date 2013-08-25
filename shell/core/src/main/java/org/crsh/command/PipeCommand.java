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

import org.crsh.io.Filter;
import org.crsh.util.TypeResolver;

import java.io.IOException;

/**
 * A pipe command.
 *
 * @param <C> the consumed generic type
 * @param <P> the produced generic type
 */
public abstract class PipeCommand<C, P> implements Filter<C, P, InvocationContext<P>> {

  /** . */
  protected InvocationContext<P> context;

  public final boolean isPiped() {
    return context.isPiped();
  }

  public final Class<P> getProducedType() {
    return (Class<P>)TypeResolver.resolveToClass(getClass(), PipeCommand.class, 1);
  }

  public final Class<C> getConsumedType() {
    return (Class<C>)TypeResolver.resolveToClass(getClass(), PipeCommand.class, 0);
  }

  public void open(InvocationContext<P> consumer) {
    this.context = consumer;

    //
    open();
  }

  /**
   * Open pipe.
   */
  public void open() throws ScriptException {
  }

  /**
   * Extends the throw clause of the {@link org.crsh.io.Consumer#provide(Object)} method.
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
    context.flush();
  }

  /**
   * Close pipe.
   *
   * @throws ScriptException any script exception
   */
  public void close() throws ScriptException {
  }
}
