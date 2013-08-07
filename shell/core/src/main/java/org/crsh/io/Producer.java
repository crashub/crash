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

package org.crsh.io;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * A producer that produces elements in a specific consumer.
 *
 * @param <P> the produced element generic type
 * @param <C> the consumer element generic type
 */
public interface Producer<P, C extends Consumer<? super P>> extends Closeable {

  /**
   * Returns the class of the produced type.
   *
   * @return the produced type
   */
  Class<P> getProducedType();

  /**
   * Open the producer with the specified consumer.
   *
   * @param consumer the consumer
   */
  void open(C consumer);

  /**
   * Close the producer.
   *
   * @throws IOException any io exception
   * @throws UndeclaredThrowableException anything other kind of exception
   */
  void close() throws IOException, UndeclaredThrowableException;

}
