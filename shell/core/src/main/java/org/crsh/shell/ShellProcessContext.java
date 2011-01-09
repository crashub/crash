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

package org.crsh.shell;

/**
 * The process context is the main interaction interface between a shell process and its context.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ShellProcessContext {

  /**
   * Returns the term width in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  /**
   * This method is invoked before the process work begins. It provides the process callback that
   * can be used during the process execution by the context.
   *
   * @param process the process
   */
  void begin(ShellProcess process);

  /**
   * A callback made by the process when it needs to read a line of text on the term.
   *
   * @param msg the message to display prior reading the term
   * @param echo whether the input line should be echoed or not
   * @return the line read or null if no line was possible to be read
   */
  String readLine(String msg, boolean echo);

  /**
   * This method is invoked when the process ends.
   *
   * @param response the shell response
   */
  void end(ShellResponse response);

}
