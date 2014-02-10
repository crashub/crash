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

package org.crsh.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class AbstractSocketClient implements Closeable {

  /** . */
  private int port;

  /** . */
  private Socket socket;

  /** . */
  private InputStream in;

  /** . */
  private OutputStream out;

  public AbstractSocketClient(int port) {
    this.port = port;
  }

  public final void connect() throws IOException {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(port));
    InputStream in = socket.getInputStream();
    OutputStream out = socket.getOutputStream();

    //
    this.socket = socket;
    this.in = in;
    this.out = out;

    //
    handle(in ,out);
  }

  protected abstract void handle(InputStream in, OutputStream out) throws IOException;

  public final void close() {
    try {
      Utils.close(socket);
      Utils.close(in);
      Utils.close(out);
    }
    finally {
      this.socket = null;
      this.in = null;
      this.out = null;
    }
  }
}
