package org.crsh.term.spi.net;

import org.crsh.term.*;
import org.crsh.term.spi.TermIO;
import org.crsh.text.Data;
import org.crsh.text.DataFragment;
import org.crsh.text.Style;
import org.crsh.util.AbstractSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TermIOClient extends AbstractSocketClient implements TermIO {

  /** . */
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  /** . */
  private byte[] bytes = new byte[2000];

  /** . */
  private ByteBuffer buffer = ByteBuffer.wrap(bytes);

  /** . */
  private InputStream in;

  /** . */
  private OutputStream out;

  public TermIOClient(int port) {
    super(port);
  }

  @Override
  protected void handle(InputStream in, OutputStream out) throws IOException {
    this.in = in;
    this.out = out;
  }

  private void put(byte b) {
    if (buffer.remaining() == 0) {
      byte[] bytesCopy = new byte[bytes.length * 2 + 1];
      System.arraycopy(bytes, 0, bytesCopy, 0, bytes.length);
      ByteBuffer bufferCopy = ByteBuffer.wrap(bytesCopy);
      bufferCopy.position(buffer.position());

      //
      bytes = bytesCopy;
      buffer = bufferCopy;
    }
    buffer.put(b);
  }


  private int _read(byte[] buffer, int off, int len) throws IOException, Done {
    int b = in.read(buffer, off, len);
    if (b == -1) {
      throw new Done();
    }
    return b;
  }

  private byte _read() throws IOException, Done {
    int b = in.read();
    if (b == -1) {
      throw new Done();
    }
    return (byte)b;
  }

  public int read() throws IOException {
    try {
      out.write(0);
      out.flush();
      byte b = _read();
      CodeType codeType = CodeType.valueOf(b);
      if (codeType == null) {
        throw new UnsupportedOperationException("todo " + b);
      } else if (codeType == CodeType.CHAR) {
        byte b1 = _read();
        byte b2 = _read();
        return (b1 << 8) + b2;
      } else {
        return codeType.ordinal() << 16;
      }
    } catch (Done done) {
      throw new UnsupportedOperationException("implement me", done);
    }
  }

  public int getWidth() {
    String width = getProperty("width");
    return Integer.parseInt(width);
  }

  public String getProperty(String name) {
    // We don't process empty name
    if (name.length() == 0) {
      return null;
    }
    byte[] bytes = name.getBytes(UTF_8);
    int len = bytes.length;
    if (len > 256) {
      throw new IllegalArgumentException("Property name too long : " + name);
    }
    try {
      out.write(8);
      out.write(len - 1);
      out.write(bytes);
      out.flush();
      len = _read();
      if (len == 0) {
        return null;
      } else if (len == 1) {
        return "";
      } else {
        bytes = new byte[len - 1];
        _read(bytes, 0, bytes.length);
        return new String(bytes, 0, bytes.length);
      }

      //
    } catch (Done done) {
      throw new UnsupportedOperationException("implement me", done);
    } catch (IOException e) {
      throw new UnsupportedOperationException("implement me", e);
    }
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

  public void flush() throws IOException {
    put((byte)7);
    out.write(bytes, 0, buffer.position());
    buffer.clear();
  }

  public void write(char c) throws IOException {
    put((byte)1);
    put((byte)((c & 0xFF00) >> 8));
    put((byte)(c & 0xFF));
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
        put((byte)2);
        put((byte)(chunkLen - 2));
        while (prev < pos) {
          char c = s.charAt(prev++);
          put((byte)((c & 0xFF00) >> 8));
          put((byte)(c & 0xFF));
        }
      }
    }
  }

  public void write(Style d) throws IOException {
  }

  public void writeDel() throws IOException {
    put((byte)3);
  }

  public void writeCRLF() throws IOException {
    put((byte)4);
  }

  public boolean moveRight(char c) throws IOException {
    put((byte)5);
    put((byte)((c & 0xFF00) >> 8));
    put((byte)(c & 0xFF));
    return true;
  }

  public boolean moveLeft() throws IOException {
    put((byte)6);
    return true;
  }
}
