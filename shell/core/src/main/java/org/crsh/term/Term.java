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

package org.crsh.term;

import org.crsh.text.ChunkSequence;

import java.io.Closeable;
import java.io.IOException;

/**
 * An high level term abstraction.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Term extends Closeable {


  /**
   * Returns the term width in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  /**
   * Retrieves the value of a property specified by this Term
   *
   * @param name name of the term property
   * @return value of the term property
   */
  String getProperty(String name);

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
   * Write a message on the console, the text will be appended.
   *
   *
   * @param reader the message to write
   * @throws IOException any io exception
   */
  void write(ChunkSequence reader) throws IOException;

  /**
   * Returns the insert buffer, any char appended in the returned appendable will translate into an
   * insertion in the buffer.
   *
   * @return the insert buffer.
   */
  Appendable getInsertBuffer();

  /**
   * Returns the current buffer;
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

  /**
   * Close the term. If threads are blocked in the {@link #read()} operation, those thread should be unblocked.
   */
  void close();

}
