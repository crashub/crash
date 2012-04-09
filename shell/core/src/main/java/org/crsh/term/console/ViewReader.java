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

package org.crsh.term.console;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ViewReader implements Appendable {

  public abstract ViewReader append(char c) throws IOException;

  public abstract ViewReader append(CharSequence s) throws IOException;

  public abstract ViewReader append(CharSequence csq, int start, int end) throws IOException;

  /**
   * Replace all the characters before the cursor by the provided char sequence.
   *
   * @param s the new char sequence
   * @return the l
   * @throws IOException any IOException
   */
  public abstract CharSequence replace(CharSequence s) throws IOException;

  /**
   * Delete the char under the cursor or return -1 if no char was deleted.
   *
   * @return the deleted char
   * @throws IOException any IOException
   */
  public abstract int del() throws IOException;

  public abstract boolean moveRight() throws IOException;

  public abstract boolean moveLeft() throws IOException;
}
