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

import java.io.IOException;

/**
 * The screen context extends the {@link Screenable} and add information about the screen.
 */
public interface ScreenContext extends Screenable {

  /**
   * Returns the screen width in chars. When the value is not positive it means
   * the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  /**
   * Returns the screen height in chars. When the value is not positive it means
   * the value could not be determined.
   *
   * @return the term height
   */
  int getHeight();

  /**
   * Flush the stream.
   *
   * @throws IOException any io exception
   */
  void flush() throws IOException;

}
