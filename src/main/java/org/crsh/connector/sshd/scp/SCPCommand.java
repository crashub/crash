/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.connector.sshd.scp;

import org.crsh.connector.sshd.AbstractCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Three internal options in SCP:
 * <ul>
 * <li><code>-f</code> (from) indicates source mode</li>
 * <li><code>-t</code> (to) indicates sink mode</li>
 * <li><code>-d</code> indicates that the target is expected to be a directory</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SCPCommand extends AbstractCommand {

  /** . */
  protected static final int OK = 0;

  /** . */
  protected static final int ERROR = 2;

  public void file(String name, byte[] content) throws IOException {
    out.write("C0644 ".getBytes());
    out.write(Integer.toString(content.length).getBytes());
    out.write(" ".getBytes());
    out.write(name.getBytes());
    out.write("\n".getBytes());
    out.flush();
    readAck();
    out.write(content);
    ack();
    readAck();
  }

  public void startDirectory(String name) throws IOException {
    out.write("D0755 0 ".getBytes());
    out.write(name.getBytes());
    out.write("\n".getBytes());
    out.flush();
    readAck();
  }

  /**
   * Read from the input stream an exact amount of bytes.
   *
   * @param length the expected data length to read
   * @return an input stream for reading
   * @throws IOException any io exception
   */
  InputStream read(final int length) throws IOException {
    System.out.println("Returning stream for length " + length);
    return new InputStream() {

      /** How many we've read so far. */
      int count = 0;

      @Override
      public int read() throws IOException {
        if (count < length) {
          int value = in.read();
          if (value == -1) {
            throw new IOException("Abnormal end of stream reached");
          }
          count++;
          return value;
        } else {
          return -1;
        }
      }
    };
  }

  public void endDirectory() throws IOException {
    out.write("E\n".getBytes());
    out.flush();
    readAck();
  }

  protected void ack() throws IOException {
      out.write(0);
      out.flush();
  }

  protected void readAck() throws IOException {
    int c = in.read();
    switch (c) {
      case 0:
        break;
      case 1:
        System.out.println("Received warning: " + readLine());
        break;
      case 2:
        throw new IOException("Received nack: " + readLine());
    }
  }

  protected String readLine() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (true) {
      int c = in.read();
      if (c == '\n') {
        return baos.toString();
      }
      else if (c == -1) {
        throw new IOException("End of stream");
      }
      else {
        baos.write(c);
      }
    }
  }
}
