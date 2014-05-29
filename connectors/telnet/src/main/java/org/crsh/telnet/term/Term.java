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

package org.crsh.telnet.term;

import org.crsh.text.ScreenContext;

import java.io.Closeable;
import java.io.IOException;

public interface Term extends Closeable, ScreenContext {

  /**
   * Retrieves the value of a property specified by this Term
   *
   * @param name name of the term property
   * @return value of the term property
   */
  String getProperty(String name);

  /**
   * Take control of the alternate buffer. When the alternate buffer is already used
   * nothing happens. The buffer switch should occur when then {@link #flush()} method
   * is invoked.
   *
   * @return true if the alternate buffer is shown
   */
  boolean takeAlternateBuffer() throws IOException;

  /**
   * Release control of the alternate buffer. When the normal buffer is already used
   * nothing happens. The buffer switch should occur when then {@link #flush()} method
   * is invoked.
   *
   * @return true if the usual buffer is shown
   */
  boolean releaseAlternateBuffer() throws IOException;

  /**
   * Set the echo mode on the term.
   *
   * @param echo the echo mode
   */
  void setEcho(boolean echo);

  /**
   * Read the next term event. This operation is a blocking operation that blocks until data is available or until
   * term is closed.
   *
   * @return the next term event
   * @throws IOException any io exception
   */
  TermEvent read() throws IOException;

  /**
   * Returns the direct buffer, any char appended in the returned appendable will translate into an
   * insertion in the buffer.
   *
   * @return the insert buffer.
   */
  Appendable getDirectBuffer();

  /**
   * Returns the current buffer content to the cursor;
   *
   * @return the buffer
   */
  CharSequence getBuffer();

  /**
   * Append a line to the term history.
   *
   * @param line the history line to append
   */
  void addToHistory(CharSequence line);

}
