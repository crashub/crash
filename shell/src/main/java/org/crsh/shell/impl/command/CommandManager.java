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
package org.crsh.shell.impl.command;

import org.crsh.command.CommandCreationException;

import java.util.HashMap;
import java.util.Set;

/** @author Julien Viet */
public interface CommandManager {

  /**
   * Return true if this command manager is active. Implementation can decide based on the runtime, for instance the
   * Groovy REPL can be inactive when Groovy is not available at runtime.
   *
   * @return the active status
   */
  boolean isActive();

  /**
   * Returns the set of extensions managed by this implementation.
   *
   * @return the set of extensions, for instance ("groovy")
   */
  Set<String> getExtensions();

  /**
   * Resolve a command for the specified command name.
   *
   * @param name the command name
   * @param source the command source  @return the command or null if no command can be resolved
   * @throws CommandCreationException when the command exists but cannot be created
   * @throws NullPointerException if the command name is null
   */
  CommandResolution resolveCommand(String name, byte[] source) throws CommandCreationException, NullPointerException;

  void init(HashMap<String, Object> session);

  void destroy(HashMap<String, Object> session);

  String doCallBack(HashMap<String, Object> session, String name, String defaultValue);

}
