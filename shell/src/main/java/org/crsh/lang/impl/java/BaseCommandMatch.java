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
package org.crsh.lang.impl.java;

import org.crsh.cli.impl.lang.Instance;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandMatch;
import org.crsh.shell.impl.command.spi.CommandInvoker;

/**
* @author Julien Viet
*/
abstract class BaseCommandMatch<T extends BaseCommand, C, P> extends CommandMatch<C, P> {

  /** . */
  private final ClassShellCommand<T> baseShellCommand;

  public BaseCommandMatch(ClassShellCommand<T> baseShellCommand) {
    this.baseShellCommand = baseShellCommand;
  }

  public CommandInvoker<C, P> getInvoker() throws CommandException {
    final T command = baseShellCommand.createCommand();
    return getInvoker(command);
  }

  abstract class BaseInvoker extends CommandInvoker<C, P> implements Instance<T> {

    final T command;

    protected BaseInvoker(T command) {
      this.command = command;
    }

    @Override
    public <U> U resolve(Class<U> type) {
      if (type.equals(InvocationContext.class)) {
        return type.cast(command.peekContext());
      } else {
        return null;
      }
    }

    @Override
    public T get() throws Exception {
      return command;
    }
  }

  abstract BaseInvoker getInvoker(T command) throws CommandException;
}
