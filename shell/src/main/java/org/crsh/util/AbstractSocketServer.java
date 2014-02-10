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
import java.net.ServerSocket;
import java.net.Socket;

public abstract class AbstractSocketServer implements Closeable {

  /** . */
  private final int bindingPort;

  /** . */
  private ServerSocket socketServer;

  /** . */
  private Socket socket;

  /** . */
  private InputStream in;

  /** . */
  private OutputStream out;

  /** . */
  private int port;

  public AbstractSocketServer(int bindingPort) {
    this.bindingPort = bindingPort;
  }

  public final int getBindingPort() {
    return socketServer.getLocalPort();
  }

  public final int getPort() {
    return port;
  }

  public final int bind() throws IOException {
    ServerSocket socketServer = new ServerSocket();
    socketServer.bind(new InetSocketAddress(bindingPort));
    int port = socketServer.getLocalPort();

    //
    this.socketServer = socketServer;
    this.port = port;

    //
    return port;
  }

  public final void accept() throws IOException {
    if (socketServer == null) {
      throw new IllegalStateException();
    }

    //
    this.socket = socketServer.accept();
    this.in = socket.getInputStream();
    this.out = socket.getOutputStream();

    //
    handle(in, out);
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
