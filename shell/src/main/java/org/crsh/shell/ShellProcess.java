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

/**
 * A shell process.
 */
public interface ShellProcess {

  /**
   * Begin the process. The client of this method should make no assumption whether the process is executed
   * in a synchronous or asynchronous manner. The process will be termined when the process signals it
   * with an invocation of the {@link ShellProcessContext#end(ShellResponse)} method.
   *
   * @param processContext the process context
   * @throws IllegalStateException if the process cannot be executed for some reason
   */
  void execute(ShellProcessContext processContext) throws IllegalStateException;

  /**
   * Signals the process it should be cancelled.
   */
  void cancel();

}
