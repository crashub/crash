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
package org.crsh.lang;

import org.crsh.command.CommandCreationException;
import org.crsh.command.ShellCommand;
import org.crsh.lang.groovy.shell.GroovyCommandManager;
import org.crsh.plugin.PluginContext;

import java.util.HashMap;

/** @author Julien Viet */
public abstract class CommandManager {

  public static CommandManager create(PluginContext context) {
    return new GroovyCommandManager(context);
  }

  /**
   * Resolve a command for the specified command name.
   *
   * @param name the command name
   * @return the command or null if no command can be resolved
   * @throws CommandCreationException when the command exists but cannot be created
   * @throws NullPointerException if the command name is null
   */
  public abstract ShellCommand resolveCommand(String name) throws CommandCreationException, NullPointerException;

  public abstract void init(HashMap<String, Object> session);

  public abstract void destroy(HashMap<String, Object> session);

  public abstract String doCallBack(HashMap<String, Object> session, String name, String defaultValue);

}
