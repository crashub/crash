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

import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractCommand implements Command, SessionAware {

  /** . */
  protected InputStream in;

  /** . */
  protected OutputStream out;

  /** . */
  protected OutputStream err;

  /** . */
  protected ExitCallback callback;

  /** . */
  protected ServerSession session;

  public final void setInputStream(InputStream in) {
    this.in = in;
  }

  public final void setOutputStream(OutputStream out) {
    this.out = out;
  }

  public final void setErrorStream(OutputStream err) {
    this.err = err;
  }

  public final void setExitCallback(ExitCallback callback) {
    this.callback = callback;
  }

  public void setSession(ServerSession session) {
    this.session = session;
  }
}
