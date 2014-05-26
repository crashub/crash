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

import org.crsh.lang.spi.Repl;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.plugin.PluginContext;

import java.util.Map;

/** @author Julien Viet */
public interface ShellSession extends Map<String, Object> {

  PluginContext getContext();

  Iterable<Map.Entry<String, String>> getCommands();

  Command<?> getCommand(String name) throws CommandException;

  /**
   * @return the current repl of this session
   */
  Repl getRepl();

  /**
   * Set the current repl of this session.
   *
   * @param repl the new repl
   * @throws NullPointerException if the repl is null
   */
  void setRepl(Repl repl) throws NullPointerException;

}
