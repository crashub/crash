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
package org.crsh.ssh.term;

import org.apache.sshd.server.Environment;

import java.io.IOException;
import java.security.Principal;

public class CRaSHCommand extends AbstractCommand implements Runnable {

  /** . */
  private final CRaSHCommandFactory factory;

  /** . */
  private Thread thread;

  public CRaSHCommand(CRaSHCommandFactory factory) {
    this.factory = factory;
  }

  /** . */
  private SSHContext context;

  /** . */
  private SSHIO io;

  public void start(Environment env) throws IOException {

    //
    context = new SSHContext(env);
    io = new SSHIO(this);

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  public SSHContext getContext() {
    return context;
  }

  public void destroy() {
    io.closed.set(true);
    thread.interrupt();
  }

  public void run() {
    try {
      final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      Principal user = new Principal() {
        public String getName() {
          return userName;
        }
      };
      factory.handler.handle(io, user);
    } finally {
      callback.onExit(0);
    }
  }
}


