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

import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;
import org.crsh.text.Style;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHIO implements TermIO {

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int UP = 1001;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int DOWN = 1002;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int RIGHT = 1003;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int LEFT = 1004;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int HANDLED = 1305;

  /** . */
  private static final int BACKWARD_WORD = -1;

  /** . */
  private static final int FORWARD_WORD = -2;

  /** . */
  private static final Logger log = Logger.getLogger(SSHIO.class.getName());

  /** . */
  private final Reader reader;

  /** . */
  private final Writer writer;

  /** . */
  private static final int STATUS_NORMAL = 0;

  /** . */
  private static final int STATUS_READ_ESC_1 = 1;

  /** . */
  private static final int STATUS_READ_ESC_2 = 2;

  /** . */
  private int status;

  /** . */
  private final CRaSHCommand command;

  /** . */
  final AtomicBoolean closed;

  /** . */
  private boolean useAlternate;

  public SSHIO(CRaSHCommand command) {
    this.command = command;
    this.writer = new OutputStreamWriter(command.out);
    this.reader = new InputStreamReader(command.in);
    this.status = STATUS_NORMAL;
    this.closed = new AtomicBoolean(false);
    this.useAlternate = false;
  }

  public int read() throws IOException {
    while (true) {
      if (closed.get()) {
        return HANDLED;
      } else {
        int r;
        try {
          r = reader.read();
        } catch (IOException e) {
          // This would likely happen when the client close the connection
          // when we are blocked on a read operation by the
          // CRaShCommand#destroy() method
          close();
          return HANDLED;
        }
        if (r == -1) {
          return HANDLED;
        } else {
          switch (status) {
            case STATUS_NORMAL:
              if (r == 27) {
                status = STATUS_READ_ESC_1;
              } else {
                return r;
              }
              break;
            case STATUS_READ_ESC_1:
              if (r == 91) {
                status = STATUS_READ_ESC_2;
              } else if (r == 98) {
                status = STATUS_NORMAL;
                return BACKWARD_WORD;
              } else if (r == 102) {
                status = STATUS_NORMAL;
                return FORWARD_WORD;
              } else {
                status = STATUS_NORMAL;
                log.log(Level.SEVERE, "Unrecognized stream data " + r + " after reading ESC code");
              }
              break;
            case STATUS_READ_ESC_2:
              status = STATUS_NORMAL;
              switch (r) {
                case 65:
                  return UP;
                case 66:
                  return DOWN;
                case 67:
                  return RIGHT;
                case 68:
                  return LEFT;
                default:
                  log.log(Level.SEVERE, "Unrecognized stream data " + r + " after reading ESC+91 code");
                  break;
              }
          }
        }
      }
    }
  }

  public int getWidth() {
    return command.getContext().getWidth();
  }

  public int getHeight() {
    return command.getContext().getHeight();
  }

  public String getProperty(String name) {
    return command.getContext().getProperty(name);
  }

  public boolean takeAlternateBuffer() throws IOException {
    if (!useAlternate) {
      useAlternate = true;
      writer.write("\033[?47h");
    }
    return true;
  }

  public boolean releaseAlternateBuffer() throws IOException {
    if (useAlternate) {
      useAlternate = false;
      writer.write("\033[?47l"); // Switches back to the normal screen
    }
    return true;
  }

  public CodeType decode(int code) {
    if (code == command.getContext().verase) {
      return CodeType.BACKSPACE;
    } else {
      switch (code) {
        case HANDLED:
          return CodeType.CLOSE;
        case 1:
          return CodeType.BEGINNING_OF_LINE;
        case 5:
          return CodeType.END_OF_LINE;
        case 3:
          return CodeType.BREAK;
        case 9:
          return CodeType.TAB;
        case UP:
          return CodeType.UP;
        case DOWN:
          return CodeType.DOWN;
        case LEFT:
          return CodeType.LEFT;
        case RIGHT:
          return CodeType.RIGHT;
        case BACKWARD_WORD:
          return CodeType.BACKWARD_WORD;
        case FORWARD_WORD:
          return CodeType.FORWARD_WORD;
        default:
          return CodeType.CHAR;
      }
    }
  }

  public void close() {
    if (closed.get()) {
      log.log(Level.FINE, "Attempt to closed again");
    } else {
      log.log(Level.FINE, "Closing SSHIO");
      command.session.close(false);
    }
  }

  public void flush() throws IOException {
    writer.flush();
  }

  public void write(CharSequence s) throws IOException {
    writer.write(s.toString());
  }

  public void write(char c) throws IOException {
    writer.write(c);
  }

  public void write(Style d) throws IOException {
    d.writeAnsiTo(writer);
  }

  public void writeDel() throws IOException {
    writer.write("\033[D \033[D");
  }

  public void writeCRLF() throws IOException {
    writer.write("\r\n");
  }

  public boolean moveRight(char c) throws IOException {
    writer.write(c);
    return true;
  }

  public boolean moveLeft() throws IOException {
    writer.write("\033[");
    writer.write("1D");
    return true;
  }

  public void cls() throws IOException {
    writer.write("\033[");
    writer.write("2J");
    writer.write("\033[");
    writer.write("1;1H");
  }
}
