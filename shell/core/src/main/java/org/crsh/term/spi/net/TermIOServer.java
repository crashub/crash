package org.crsh.term.spi.net;

import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;
import org.crsh.util.Safe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TermIOServer {

  /** . */
  private final TermIO delegate;

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

  public TermIOServer(TermIO delegate, int bindingPort) {
    this.delegate = delegate;
    this.bindingPort = bindingPort;
  }

  public int getBindingPort() {
    return socketServer.getLocalPort();
  }

  public int getPort() {
    return port;
  }

  public int bind() throws IOException {
    ServerSocket socketServer = new ServerSocket();
    socketServer.bind(new InetSocketAddress(bindingPort));
    int port = socketServer.getLocalPort();

    //
    this.socketServer = socketServer;
    this.port = port;

    //
    return port;
  }

  public void accept() throws IOException {
    if  (socketServer == null) {
      throw new IllegalStateException();
    }

    //
    this.socket = socketServer.accept();
    this.in = socket.getInputStream();
    this.out = socket.getOutputStream();
  }

  private static class Done extends Throwable {
  }

  private byte read() throws IOException, Done {
    int b = in.read();
    if (b == -1) {
      throw new Done();
    }
    return (byte)b;
  }

  private int read(byte[] buffer, int off, int len) throws IOException, Done {
    int b = in.read(buffer, off, len);
    if (b == -1) {
      throw new Done();
    }
    return b;
  }

  private void write(byte b) throws IOException, Done {
    out.write(b);
  }

  private void write(byte[] bytes) throws IOException, Done {
    out.write(bytes);
  }

  private void flush() throws IOException, Done {
    out.flush();
  }

  public boolean execute() throws IOException, IllegalStateException {
    if (socket == null) {
      throw new IllegalStateException("No connection");
    }
    try {
      iterate();
      return true;
    } catch (Done ignore) {
      Safe.close(in);
      Safe.close(out);
      Safe.close(socket);
      in = null;
      out = null;
      socket = null;
      return false;
    }
  }

  private void iterate() throws IOException, Done {
    byte b = read();
    if (b == 0) {
      int code = delegate.read();
      CodeType codeType = delegate.decode(code);
      byte ordinal = (byte) codeType.ordinal();
      if (codeType == CodeType.CHAR) {
        write(new byte[]{ordinal, (byte)((code & 0xFF00) >> 8), (byte)((code & 0xFF))});
      } else {
        write(ordinal);
      }
      flush();
    } else if (b == 1) {
      int b1 = in.read();
      int b2 = in.read();
      char c = (char)((b1 << 8) + b2);
      delegate.write(c);
    } else if (b == 2) {
      b = read();
      int remaining = (b + 2) * 2;
      int offset = 0;
      byte[] buffer = new byte[remaining];
      while (remaining > 0) {
        int r = read(buffer, offset, remaining);
        offset += r;
        remaining -= r;
      }
      char[] chars = new char[buffer.length / 2];
      int index = 0;
      for (int i = 0;i < chars.length;i++) {
        int high = buffer[index++];
        int low = buffer[index++];
        chars[i] = (char)((high << 8) + low);
      }
      String s = new String(chars);
      delegate.write(s);
    } else if (b == 3) {
      delegate.writeDel();
    } else if (b == 4) {
      delegate.writeCRLF();
    } else if (b == 5) {
      int b1 = in.read();
      int b2 = in.read();
      char c = (char)((b1 << 8) + b2);
      delegate.moveRight(c);
    } else if (b == 6) {
      delegate.moveLeft();
    } else if (b == 7) {
      delegate.flush();
    } else {
      throw new UnsupportedOperationException("cannot handle " + b);
    }
  }
}
