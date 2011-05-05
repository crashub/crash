/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.shell.impl;

import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class CreateCommandException extends Exception {

  /** . */
  private final ShellResponse response;

  CreateCommandException(ShellResponse response) {
    this.response = response;
  }

  CreateCommandException(ErrorType errorType, String message) {
    super(message);

    //
    this.response = new ShellResponse.Error(errorType, message);
  }

  CreateCommandException(ErrorType errorType, String message, Throwable cause) {
    super(message);

    //
    this.response = new ShellResponse.Error(errorType, message, cause);
  }

  CreateCommandException(ErrorType errorType, Throwable cause) {
    super(cause);

    //
    this.response = new ShellResponse.Error(errorType, cause);
  }

  public ShellResponse getResponse() {
    return response;
  }
}
