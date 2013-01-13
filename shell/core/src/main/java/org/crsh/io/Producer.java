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

public interface Producer<P> extends Closeable {

  /**
   * Returns the class of the produced type.
   *
   * @return the produced type
   */
  Class<P> getProducedType();

  /**
   * Set the pipe status.
   *
   * @param piped the piped status
   */
  void setPiped(boolean piped);

  /**
   * Open the producer with the specified context.
   *
   * @param context the context
   */
  void open(InteractionContext<P> context);

  /**
   * Close the producer.
   */
  void close();

}
