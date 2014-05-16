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

import org.crsh.cli.descriptor.Format;
import org.crsh.command.BaseCommand;
import org.crsh.lang.java.ShellCommandImpl;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.system.help;
import org.crsh.shell.impl.command.system.repl;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class CRaSH {

  /** . */
  private static final HashMap<String, Class<? extends BaseCommand>> systemCommands = new HashMap<String, Class<? extends BaseCommand>>();

  static {
    systemCommands.put("help", help.class);
    systemCommands.put("repl", repl.class);
  }

  /** . */
  final PluginContext context;

  /** . */
  final ScriptResolver resolver;

  /**
   * Create a new CRaSH.
   *
   * @param context the plugin context
   * @throws NullPointerException if the context argument is null
   */
  public CRaSH(PluginContext context) throws NullPointerException {
    this.context = context;
    this.resolver = new ScriptResolver(context);
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
   * Attempt to obtain a command description. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command description
   * @throws org.crsh.shell.impl.command.spi.CommandCreationException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public String getCommandDescription(String name) throws CommandCreationException, NullPointerException {
    CommandResolution resolution = resolveCommand(name);
    return resolution != null ? resolution.getDescription() : null;
  }

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.shell.impl.command.spi.CommandCreationException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand<?> getCommand(String name) throws CommandCreationException, NullPointerException {
    CommandResolution resolution = resolveCommand(name);
    return resolution != null ? resolution.getCommand() : null;
  }

  public CommandResolution resolveCommand(String name) throws CommandCreationException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    final Class<? extends BaseCommand> systemCommand = systemCommands.get(name);
    if (systemCommand != null) {
      return createCommand(systemCommand);
    } else {
      return resolver.resolveCommand(name);
    }
  }

  public Iterable<String> getCommandNames() {
    LinkedHashSet<String> names = new LinkedHashSet<String>(systemCommands.keySet());
    for (String name : resolver.getCommandNames()) {
      names.add(name);
    }
    return names;
  }

  private <C extends BaseCommand> CommandResolution createCommand(final Class<C> commandClass) {
    return new CommandResolution() {
      final ShellCommandImpl<C> shellCommand = new ShellCommandImpl<C>(commandClass);
      @Override
      public String getDescription() {
        return shellCommand.describe(commandClass.getSimpleName(), Format.DESCRIBE);
      }
      @Override
      public ShellCommand<?> getCommand() throws CommandCreationException {
        return shellCommand;
      }
    };
  }
}
