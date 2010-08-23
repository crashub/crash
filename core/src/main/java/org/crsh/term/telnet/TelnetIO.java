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

package org.crsh.term.telnet;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import org.crsh.term.CodeType;
import org.crsh.term.TermIO;
import java.io.IOException;

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
    return termIO.read();
  }

  public CodeType getType(int code) {
    switch (code) {
      case TerminalIO.DELETE:
      case TerminalIO.BACKSPACE:
        return CodeType.DELETE;
      case TerminalIO.UP:
        return CodeType.UP;
      case TerminalIO.DOWN:
        return CodeType.DOWN;
      case TerminalIO.RIGHT:
        return CodeType.RIGHT;
      case TerminalIO.LEFT:
        return CodeType.LEFT;
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

  public void writeDel() throws IOException {
    termIO.moveLeft(1);
    termIO.write(' ');
    termIO.moveLeft(1);
    termIO.flush();
  }

  public void writeCRLF() throws IOException {
    termIO.write("\r\n");
  }

  public boolean moveRight() throws IOException {
    termIO.moveRight(1);
    return true;
  }

  public boolean moveLeft() throws IOException {
    termIO.moveLeft(1);
    return true;
  }
}
