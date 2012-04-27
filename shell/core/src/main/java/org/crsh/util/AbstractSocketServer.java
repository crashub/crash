package org.crsh.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
      Safe.close(socket);
      Safe.close(in);
      Safe.close(out);
    }
    finally {
      this.socket = null;
      this.in = null;
      this.out = null;
    }
  }
}
