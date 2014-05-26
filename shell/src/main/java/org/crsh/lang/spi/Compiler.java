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
package org.crsh.lang.spi;

import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.CommandException;

import java.util.Set;

/**
 *
 *
 * @author Julien Viet
 */
public interface Compiler {

  /**
   * Returns the set of extensions managed by this implementation.
   *
   * @return the set of extensions, for instance ("groovy")
   */
  Set<String> getExtensions();

  /**
   * Compile a command..
   *
   * @param name the command name
   * @param source the command source  @return the command or null if no command can be resolved
   * @throws org.crsh.shell.impl.command.spi.CommandException when the command exists but cannot be created
   * @throws NullPointerException if the command name is null
   */
  CommandResolution compileCommand(String name, byte[] source) throws CommandException, NullPointerException;

  //
  String doCallBack(ShellSession session, String name, String defaultValue);

}
