package org.crsh.term.spi.net;

import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TermIOClient implements TermIO {

  /** . */
  private int port;

  /** . */
  private Socket socket;

  /** . */
  private InputStream in;

  /** . */
  private OutputStream out;

  /** . */
  private byte[] bytes = new byte[2000];

  /** . */
  private final ByteBuffer buffer = ByteBuffer.wrap(bytes);

  public TermIOClient(int port) {
    this.port = port;
  }

  public void connect() throws IOException {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(port));
    InputStream in = socket.getInputStream();
    OutputStream out = socket.getOutputStream();

    //
    this.socket = socket;
    this.in = in;
    this.out = out;
  }

  public int read() throws IOException {
    out.write(0);
    out.flush();
    int b = in.read();
    CodeType codeType = CodeType.valueOf(b);
    if (codeType == null) {
      throw new UnsupportedOperationException("todo " + b);
    } else if (codeType == CodeType.CHAR) {
      int b1 = in.read();
      int b2 = in.read();
      return (b1 << 8) + b2;
    } else {
      return codeType.ordinal() << 16;
    }
  }

  public int getWidth() {
    return 80;
  }

  public CodeType decode(int code) {
    code &= 0xFFFF0000;
    if (code == 0) {
      return CodeType.CHAR;
    } else {
      code >>= 16;
      return CodeType.valueOf(code);
    }
  }

  public void close() {
    try {
      this.socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void flush() throws IOException {
    buffer.put((byte)7);
    out.write(bytes, 0, buffer.position());
    buffer.clear();
  }

  public void write(char c) throws IOException {
    buffer.put((byte)1);
    buffer.put((byte)((c & 0xFF00) >> 8));
    buffer.put((byte)(c & 0xFF));
  }

  public void write(String s) throws IOException {
    int prev = 0;
    int len = s.length();
    while (prev < len) {
      int pos = Math.min(len, prev + 257);
      int chunkLen = pos - prev;
      if (chunkLen == 1) {
        write(s.charAt(prev++));
      } else {
        buffer.put((byte)2);
        buffer.put((byte)(chunkLen - 2));
        while (prev < pos) {
          char c = s.charAt(prev++);
          buffer.put((byte)((c & 0xFF00) >> 8));
          buffer.put((byte)(c & 0xFF));
        }
      }
    }
  }

  public void writeDel() throws IOException {
    buffer.put((byte)3);
  }

  public void writeCRLF() throws IOException {
    buffer.put((byte)4);
  }

  public boolean moveRight(char c) throws IOException {
    buffer.put((byte)5);
    buffer.put((byte)((c & 0xFF00) >> 8));
    buffer.put((byte)(c & 0xFF));
    return true;
  }

  public boolean moveLeft() throws IOException {
    buffer.put((byte)6);
    return true;
  }
}
