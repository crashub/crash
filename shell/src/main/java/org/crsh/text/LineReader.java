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

package org.crsh.text;

/**
 * The line reader.
 */
public interface LineReader {

  /**
   * Returns true if the renderer has a next line to render.
   *
   * @return when there is at least a next line to read
   */
  boolean hasLine();

  /**
   * Renders the element.
   *
   * @param to the buffer for rendering
   * @throws IllegalStateException when there is no line to render
   */
  void renderLine(RenderAppendable to) throws IllegalStateException;

}
