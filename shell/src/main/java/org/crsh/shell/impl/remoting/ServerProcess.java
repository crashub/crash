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

package org.crsh.shell.impl.remoting;

import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;

public class ServerProcess implements ShellProcess {

  /** . */
  final ServerAutomaton server;

  /** . */
  final String line;

  /** . */
  private int status;

  ServerProcess(ServerAutomaton server, String line) {
    this.server = server;
    this.line = line;
    this.status = 0;
  }

  public void execute(ShellProcessContext processContext) throws IllegalStateException {
    if (status != 0) {
      throw new IllegalStateException();
    }
    status = 1;
    try {
      server.execute(this, processContext);
    }
    finally {
      status = 2;
    }
  }

  @Override
  public KeyHandler getKeyHandler() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public void cancel() {
    switch (status) {
      case 0:
        throw new IllegalStateException();
      case 1:
        server.cancel(this);
        break;
    }
  }
}
