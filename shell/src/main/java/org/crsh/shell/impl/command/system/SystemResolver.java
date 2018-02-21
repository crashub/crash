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
  public static final SystemResolver INSTANCE = new SystemResolver();

  /** . */
  private static final HashMap<String, Class<? extends BaseCommand>> commands = new HashMap<String, Class<? extends BaseCommand>>();

  /** . */
  private static final HashMap<String, String> descriptions = new HashMap<String, String>();

  static {
    commands.put("help", help.class);
    commands.put("repl", repl.class);
    descriptions.put("help", "provides basic help");
    descriptions.put("repl", "list the repl or change the current repl");

    descriptions.put("exit", "Exits.");
    descriptions.put("bye", "Exits, same as exit.");
  }

  private SystemResolver() {
  }

  @Override
  public Iterable<Map.Entry<String, String>> getDescriptions() {
    return descriptions.entrySet();
  }

  @Override
  public Command<?> resolveCommand(String name) throws CommandException, NullPointerException {
    final Class<? extends BaseCommand> systemCommand = commands.get(name);
    if (systemCommand != null) {
      return createCommand(systemCommand).getCommand();
    }
    return null;
  }

  private <C extends BaseCommand> CommandResolution createCommand(final Class<C> commandClass) throws CommandException {
    final ClassShellCommand<C> shellCommand;
    final String description;
    try {
      shellCommand = new ClassShellCommand<C>(commandClass);
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
