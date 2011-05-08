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

package org.crsh.telnet.term;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetIO implements TermIO {

  /** . */
  private final Connection conn;

  /** . */
  private final BasicTerminalIO termIO;

  public TelnetIO(Connection conn) {
    this.conn = conn;
    this.termIO = conn.getTerminalIO();
  }

  public int read() throws IOException {
    try {
      return termIO.read();
    }
    catch (EOFException e) {
      return TerminalIO.HANDLED;
    }
    catch (SocketException e) {
      return TerminalIO.HANDLED;
    }
  }

  public int getWidth() {
    return termIO.getColumns();
  }

  public CodeType decode(int code) {
    switch (code) {
      case 3:
        return CodeType.BREAK;
      case TerminalIO.TABULATOR:
        return CodeType.TAB;
      case TerminalIO.DELETE:
      case TerminalIO.BACKSPACE:
        return CodeType.BACKSPACE;
      case TerminalIO.UP:
        return CodeType.UP;
      case TerminalIO.DOWN:
        return CodeType.DOWN;
      case TerminalIO.RIGHT:
        return CodeType.RIGHT;
      case TerminalIO.LEFT:
        return CodeType.LEFT;
      case TerminalIO.HANDLED:
        return CodeType.CLOSE;
      default:
        return CodeType.CHAR;
    }
  }

  public void close() {
    conn.close();
  }

  public void flush() throws IOException {
    termIO.flush();
  }

  public void write(String s) throws IOException {
    termIO.write(s);
  }

  public void write(char c) throws IOException {
    termIO.write(c);
  }

  public void writeDel() throws IOException {
    termIO.moveLeft(1);
    termIO.write(' ');
    termIO.moveLeft(1);
    termIO.flush();
  }

  public void writeCRLF() throws IOException {
    termIO.write("\r\n");
  }

  public boolean moveRight(char c) throws IOException {
    termIO.moveRight(1);
    return true;
  }

  public boolean moveLeft() throws IOException {
    termIO.moveLeft(1);
    return true;
  }
}
