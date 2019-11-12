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
package org.crsh.shell.impl.command.system;

import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.command.BaseCommand;
import org.crsh.command.ShellSafety;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.shell.impl.command.spi.CommandResolver;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Julien Viet
 */
public class SystemResolver implements CommandResolver {

  /** . */
  public static final SystemResolver UNSAFE_INSTANCE = new SystemResolver(false);
  public static final SystemResolver SAFE_INSTANCE = new SystemResolver(true);

  /** . */
  private static final HashMap<String, Class<? extends BaseCommand>> unsafeCommands = new HashMap<String, Class<? extends BaseCommand>>();
  private static final HashMap<String, Class<? extends BaseCommand>> safeCommands = new HashMap<String, Class<? extends BaseCommand>>();

  /** . */
  private static final HashMap<String, String> unsafeDescriptions = new HashMap<String, String>();
  private static final HashMap<String, String> safeDescriptions = new HashMap<String, String>();

  static {
    unsafeCommands.put("help", help.class);
    unsafeCommands.put("repl", repl.class);
    unsafeDescriptions.put("help", "provides basic help (all commands).");
    unsafeDescriptions.put("repl", "list the repl or change the current repl");

    unsafeDescriptions.put("exit", "Exits.");
    unsafeDescriptions.put("bye", "Exits, same as exit.");

    safeCommands.put("help", help.class);
    safeDescriptions.put("help", "provides basic help (safe mode commands).");
  }

  private final boolean safeInstance;

  private SystemResolver(boolean safe) {
    this.safeInstance = safe;
  }

  @Override
  public Iterable<Map.Entry<String, String>> getDescriptions() {
    return safeInstance ? safeDescriptions.entrySet() : unsafeDescriptions.entrySet();
  }

  @Override
  public Command<?> resolveCommand(String name, ShellSafety shellSafety) throws CommandException, NullPointerException {
    final Class<? extends BaseCommand> systemCommand = safeInstance ? safeCommands.get(name) : unsafeCommands.get(name);
    if (systemCommand != null) {
      return createCommand(systemCommand, shellSafety).getCommand(); //++++KEEP
    }
    return null;
  }

  private <C extends BaseCommand> CommandResolution createCommand(final Class<C> commandClass, ShellSafety shellSafety) throws CommandException {
    final ClassShellCommand<C> shellCommand;
    final String description;
    try {
      shellCommand = new ClassShellCommand<C>(commandClass, shellSafety);//++++KEEP
      description = shellCommand.describe(commandClass.getSimpleName(), Format.DESCRIBE);
    }
    catch (IntrospectionException e) {
      throw new CommandException(ErrorKind.SYNTAX, "Invalid cli annotation in command " + commandClass.getSimpleName(), e);
    }
    return new CommandResolution() {
      @Override
      public String getDescription() {
        return description;
      }
      @Override
      public Command<?> getCommand() throws CommandException {
        return shellCommand;
      }
    };
  }
}
