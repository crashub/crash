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

import org.crsh.shell.Shell;
import org.crsh.util.AbstractSocketServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RemoteServer extends AbstractSocketServer {

  /** . */
  private ServerAutomaton automaton;

  public RemoteServer(int bindingPort) {
    super(bindingPort);
  }

  @Override
  protected void handle(InputStream in, OutputStream out) throws IOException {
    this.automaton = new ServerAutomaton(in, out).addCloseListener(this);
  }

  public Shell getShell() {
    return automaton;
  }
}
