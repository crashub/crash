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

import groovy.lang.Script;
import org.crsh.command.GroovyScript;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;

import java.security.Principal;

public class CRaSH {


  /** . */
  final ClassManager<? extends ShellCommand> commandManager;

  /** . */
  final ClassManager<? extends GroovyScript> scriptManager;

  /** . */
  final PluginContext context;

  /**
   * Create a new CRaSH.
   *
   * @param context the plugin context
   * @throws NullPointerException if the context argument is null
   */
  public CRaSH(PluginContext context) throws NullPointerException {
    this(
      context,
      new ClassManager<ShellCommand>(context, ResourceKind.COMMAND, ShellCommand.class, GroovyScriptCommand.class),
      new ClassManager<GroovyScript>(context, ResourceKind.LIFECYCLE, GroovyScript.class, GroovyScript.class)
    );
  }

  public CRaSH(PluginContext context, ClassManager<ShellCommand> commandManager, ClassManager<? extends GroovyScript> scriptManager) {
    this.context = context;
    this.commandManager = commandManager;
    this.scriptManager = scriptManager;
  }

  public CRaSHSession createSession(Principal user) {
    return new CRaSHSession(this, user);
  }

  /**
   * Returns the plugin context.
   *
   * @return the plugin context
   */
  public PluginContext getContext() {
    return context;
  }

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.command.NoSuchCommandException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand getCommand(String name) throws NoSuchCommandException, NullPointerException {
    return commandManager.getInstance(name);
  }
}
