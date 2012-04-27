package org.crsh.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
