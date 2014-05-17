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
import org.crsh.command.BaseCommand;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.shell.impl.command.spi.ShellCommandResolver;

import java.util.HashMap;

/**
 * @author Julien Viet
 */
public class SystemResolver implements ShellCommandResolver {

  /** . */
  public static final SystemResolver INSTANCE = new SystemResolver();

  /** . */
  private static final HashMap<String, Class<? extends BaseCommand>> systemCommands = new HashMap<String, Class<? extends BaseCommand>>();

  static {
    systemCommands.put("help", help.class);
    systemCommands.put("repl", repl.class);
  }

  private SystemResolver() {
  }

  @Override
  public Iterable<String> getCommandNames() {
    return systemCommands.keySet();
  }

  @Override
  public CommandResolution resolveCommand(String name) throws CommandCreationException, NullPointerException {
    final Class<? extends BaseCommand> systemCommand = systemCommands.get(name);
    if (systemCommand != null) {
      return createCommand(systemCommand);
    }
    return null;
  }

  private <C extends BaseCommand> CommandResolution createCommand(final Class<C> commandClass) {
    return new CommandResolution() {
      final ClassShellCommand<C> shellCommand = new ClassShellCommand<C>(commandClass);
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
