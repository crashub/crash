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

import org.crsh.term.Data;

import java.io.IOException;

/**
 * The console writer controls the output as seen by the client of the console.
 * It never changes the console internal state.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ConsoleWriter {

  /**
   * Write a char sequence to the output.
   *
   * @param s the char sequence
   * @throws IOException any io exception
   */
  public abstract void write(CharSequence s) throws IOException;

  /**
   * Write a single char to the output.
   *
   * @param c the char to write
   * @throws IOException any io exception
   */
  public abstract void write(char c) throws IOException;

  /**
   * Write some data to the output.
   *
   * @param d the data to write
   * @throws IOException any io exception
   */
  public abstract void write(Data d) throws IOException;

}
