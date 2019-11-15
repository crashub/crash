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

package org.crsh.command;

import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;

public interface InvocationContext<P> extends CommandContext<P> {

  /**
   * Returns the writer for the output.
   *
   * @return the writer
   */
  RenderPrintWriter getWriter();

  ShellSafety getShellSafety();

  /**
   * Resolve a command invoker for the specified command line.
   *
   * @param s the command line
   * @return the command invoker
   * @throws CommandException any command exception
   */
  CommandInvoker<?, ?> resolve(String s) throws CommandException;

}
