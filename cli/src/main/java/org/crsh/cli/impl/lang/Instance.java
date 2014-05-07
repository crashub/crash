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
package org.crsh.cli.impl.lang;

/**
 * @author Julien Viet
 */
public interface Instance<T> {

  /**
   * Resolve the specified contextual type to an instance or return null if no object able
   * to satisfy the class type was resolved.
   *
   * @param type the type to resolve
   * @param <T> the generic type parameter
   * @return the resolved instance
   */
  <T> T resolve(Class<T> type);

  /**
   * Return the instance, the same instance should be returned, however it can lazily be created.
   *
   * @return the instance
   * @throws Exception any exception preventing to obtain the instance
   */
  T get() throws Exception;

}
