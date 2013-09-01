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

import java.io.Flushable;
import java.io.IOException;

/**
 * Defines the interface for a consumer.
 *
 * @param <C> the consumed element generic type
 */
public interface Consumer<C> extends Flushable {

  /**
   * Provide an element.
   *
   * @param element the provided element
   */
  void provide(C element) throws IOException;

  /**
   * Returns the class of the element generic type.
   *
   * @return the consumed type
   */
  Class<C> getConsumedType();
}
