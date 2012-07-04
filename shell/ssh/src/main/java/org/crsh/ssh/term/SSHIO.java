/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.crsh.term.ANSIFontBuilder;
import org.crsh.term.CodeType;
import org.crsh.term.Data;
import org.crsh.term.FormattingData;
import org.crsh.term.DataFragment;
import org.crsh.term.spi.TermIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHIO implements TermIO {

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int UP = 1001;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int DOWN = 1002;

  /** Copied from net.wimpi.telnetd.io.TerminalIO. */
  private static final int HANDLED = 1305;

  /** . */
  private static final Logger log = LoggerFactory.getLogger(SSHIO.class);

  /** . */
  private static final char DELETE_PREV_CHAR = 8;

  /** . */
  private static final String DEL_SEQ = DELETE_PREV_CHAR + " " + DELETE_PREV_CHAR;

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
  private final ANSIFontBuilder ansiBuilder;

  public SSHIO(CRaSHCommand command) {
    this.command = command;
    this.writer = new OutputStreamWriter(command.out);
    this.reader = new InputStreamReader(command.in);
    this.status = STATUS_NORMAL;
    this.closed = new AtomicBoolean(false);
    this.ansiBuilder = new ANSIFontBuilder();
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
              } else {
                status = STATUS_NORMAL;
                log.error("Unrecognized stream data " + r + " after reading ESC code");
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
                  // Swallow RIGHT
                  break;
                case 68:
                  // Swallow LEFT
                  break;
                default:
                  log.error("Unrecognized stream data " + r + " after reading ESC+91 code");
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

  public String getProperty(String name) {
    return command.getContext().getProperty(name);
  }

  public CodeType decode(int code) {
    if (code == command.getContext().verase) {
      return CodeType.BACKSPACE;
    } else {
      switch (code) {
        case HANDLED:
          return CodeType.CLOSE;
        case 3:
          return CodeType.BREAK;
        case 9:
          return CodeType.TAB;
        case UP:
          return CodeType.UP;
        case DOWN:
          return CodeType.DOWN;
        default:
          return CodeType.CHAR;
      }
    }
  }

  public void close() {
    if (closed.get()) {
      log.debug("Attempt to closed again");
    } else {
      log.debug("Closing SSHIO");
      command.session.close(false);
    }
  }

  public void flush() throws IOException {
    writer.flush();
  }

  public void write(String s) throws IOException {
    writer.write(s);
  }

  public void write(char c) throws IOException {
    writer.write(c);
  }

  public void write(Data d) throws IOException {
    for (DataFragment f : d) {
      if (f instanceof FormattingData) {
        writer.write(ansiBuilder.build((FormattingData) f).toString());
      } else {
        writer.write(f.toString());
      }
    }
  }

  public void writeDel() throws IOException {
    writer.write(DEL_SEQ);
  }

  public void writeCRLF() throws IOException {
    writer.write("\r\n");
  }

  public boolean moveRight(char c) throws IOException {
    return false;
  }

  public boolean moveLeft() throws IOException {
    return false;
  }
}
