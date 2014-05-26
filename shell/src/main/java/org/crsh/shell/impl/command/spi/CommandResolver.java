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
package org.crsh.shell.impl.command.spi;

import java.util.Map;

/**
 * Resolve commands.
 *
 * @author Julien Viet
 */
public interface CommandResolver {

  /**
   * @return the known command names by this resolver, the returned map keys are the command names
   *         and the values are the corresponding one line description of the command
   */
  Iterable<Map.Entry<String, String>> getDescriptions();

  /**
   * Attempt to obtain a command. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return the command shell
   * @throws CommandException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  Command<?> resolveCommand(String name) throws CommandException, NullPointerException;
}
