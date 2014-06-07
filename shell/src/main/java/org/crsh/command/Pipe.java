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

import org.crsh.stream.Filter;
import org.crsh.util.Utils;

import java.io.IOException;

/**
 * A command pipe.
 *
 * @param <C> the consumed generic type
 * @param <P> the produced generic type
 */
public abstract class Pipe<C, P> implements Filter<C, P, InvocationContext<P>> {

  /** . */
  protected InvocationContext<P> context;

  public final Class<P> getProducedType() {
    return (Class<P>)Utils.resolveToClass(getClass(), Pipe.class, 1);
  }

  public final Class<C> getConsumedType() {
    return (Class<C>)Utils.resolveToClass(getClass(), Pipe.class, 0);
  }

  public void open(InvocationContext<P> consumer) throws Exception {
    this.context = consumer;

    //
    open();
  }

  /**
   * Open pipe.
   */
  public void open() throws Exception {
  }

  public void provide(C element) throws Exception {
  }

  /**
   * Flush pipe.
   *
   * @throws IOException any io exception
   */
  public void flush() throws IOException {
    context.flush();
  }

  /**
   * Close pipe.
   *
   * @throws Exception any exception
   */
  public void close() throws Exception {
    context.close();
  }
}
