/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.command;

import org.crsh.shell.io.ShellPrinter;

import java.util.Map;

/**
 * The invocation context provided to a command during the invocation phase. The invocation context provides the
 * various interactions that a command can perform with its context during its invocation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface InvocationContext<C, P> extends CommandContext {

  /**
   * Returns the term width in chars. When the value is not positive it means the value could not be determined.
   *
   * @return the term width
   */
  int getWidth();

  String readLine(String msg, boolean echo);

  ShellPrinter getWriter();

  /**
   * Returns true if the command is involved in a pipe operation and receives a stream.
   *
   * @return true if the command is involved in a pipe
   */
  boolean isPiped();

  /**
   * Returns an iterator over the stream of consumed items.
   * @return the consumed items
   * @throws IllegalStateException if the command is not involved in a pipe operation
   */
  Iterable<C> consume() throws IllegalStateException;

  void produce(P product);

}
