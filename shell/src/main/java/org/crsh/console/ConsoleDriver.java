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

package org.crsh.console;

import org.crsh.text.Style;

import java.io.Closeable;
import java.io.IOException;

/**
 * The contract between the console and the underlying stream.
 */
public interface ConsoleDriver extends Closeable {

  /**
   * Returns the term width in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  /**
   * Returns the term height in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term height
   */
  int getHeight();

  /**
   * Retrieves the value of a property specified by this TermIO
   *
   * @param name the name of the property
   * @return value of the property
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
   * Flush output.
   *
   * @throws java.io.IOException any io exception
   */
  void flush() throws IOException;

  /**
   * Write a string.
   *
   * @param s the string to write
   * @throws java.io.IOException any io exception
   */
  void write(CharSequence s) throws IOException;

  /**
   * Write a string.
   *
   * @param s the string to write
   * @param start the index of the first char
   * @param end the index of the last char
   * @throws java.io.IOException any io exception
   */
  void write(CharSequence s, int start, int end) throws IOException;

  /**
   * Write a char.
   *
   * @param c the char to write
   * @throws java.io.IOException any io exception
   */
  void write(char c) throws IOException;

  /**
   * Write a style.
   *
   * @param d the data to write
   * @throws java.io.IOException any io exception
   */
  void write(Style d) throws IOException;

  /**
   * Delete the char under the cursor.
   *
   * @throws java.io.IOException any io exception
   */
  void writeDel() throws IOException;

  /**
   * Write a CRLF.
   *
   * @throws java.io.IOException any io exception
   */
  void writeCRLF() throws IOException;

  /**
   * Clear screen.
   *
   * @throws java.io.IOException any io exception
   */
  void cls() throws IOException;

  /**
   * Move the cursor right.
   *
   * @param c the char skipped over
   * @return true if the cursor moved.
   * @throws java.io.IOException any io exception
   */
  boolean moveRight(char c) throws IOException;

  /**
   * Move the cursor left.
   *
   * @return true if the cursor moved
   * @throws java.io.IOException any io exception
   */
  boolean moveLeft() throws IOException;

}
