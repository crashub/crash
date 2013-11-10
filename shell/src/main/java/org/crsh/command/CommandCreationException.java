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

package org.crsh.command;

import org.crsh.shell.ErrorType;

public final class CommandCreationException extends Exception {

  /** . */
  private final String commandName;

  /** . */
  private final ErrorType errorType;

  public CommandCreationException(String commandName, ErrorType errorType, String message) {
    super(message);

    //
    this.commandName = commandName;
    this.errorType = errorType;
  }

  public CommandCreationException(String commandName, ErrorType errorType, String message, Throwable cause) {
    super(message, cause);

    //
    this.commandName = commandName;
    this.errorType = errorType;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public String getCommandName() {
    return commandName;
  }

  @Override
  public String getMessage() {
    return "Could not create command " + commandName + ": " + super.getMessage();
  }
}
