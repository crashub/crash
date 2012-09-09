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

package org.crsh.shell;

import org.crsh.text.Chunk;
import org.crsh.text.ChunkWriter;

import java.io.IOException;

public interface ShellProcessContext extends ChunkWriter {

  /**
   * Returns the term width in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  /**
   * Returns the property defined within this context.
   *
   * @param name the name of the property
   * @return the value of the property
   */
  String getProperty(String name);

  /**
   * A callback made by the process when it needs to read a line of text on the term.
   *
   * @param msg the message to display prior reading the term
   * @param echo whether the input line should be echoed or not
   * @return the line read or null if no line was possible to be read
   */
  String readLine(String msg, boolean echo);

  /**
   * Write a chunk on the process output.
   *
   * @param chunk the chunk to write
   * @throws NullPointerException if the chunk object is null
   * @throws IOException any io exception
   */
  void write(Chunk chunk) throws NullPointerException, IOException;

  /**
   * Flush the text buffer to the context.
   */
  void flush();

  /**
   * This method is invoked when the process ends.
   *
   * @param response the shell response
   */
  void end(ShellResponse response);
}
