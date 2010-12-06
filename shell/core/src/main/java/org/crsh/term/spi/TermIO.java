/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.term.spi;

import org.crsh.term.CodeType;

import java.io.IOException;

/**
 * The input/output of a term.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface TermIO {

  /**
   * Reads an input value.
   *
   * @return the value read
   * @throws IOException any io exception
   */
  int read() throws IOException;

  /**
   * Decode the intput value.
   *
   * @param code the code
   * @return the input value type
   */
  CodeType decode(int code);

  /**
   * Close the input/output.
   */
  void close();

  /**
   * Flush output.
   *
   * @throws IOException any io exception
   */
  void flush() throws IOException;

  /**
   * Write a string.
   *
   * @param s the string to write
   * @throws IOException any io exception
   */
  void write(String s) throws IOException;

  /**
   * Write a char.
   *
   * @param c the char to write
   * @throws IOException any io exception
   */
  void write(char c) throws IOException;

  /**
   * Delete the char.
   *
   * @throws IOException any io exception
   */
  void writeDel() throws IOException;

  /**
   * Write a CRLF.
   *
   * @throws IOException any io exception
   */
  void writeCRLF() throws IOException;

  /**
   * Move the cursor right.
   *
   * @return true if the cursor moved.
   * @throws IOException any io exception
   */
  boolean moveRight() throws IOException;

  /**
   * Move the cursor left.
   *
   * @return true if the cursor moved
   * @throws IOException any io exception
   */
  boolean moveLeft() throws IOException;
}
