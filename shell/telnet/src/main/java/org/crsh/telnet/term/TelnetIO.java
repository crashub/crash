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

package org.crsh.telnet.term;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionData;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.term.*;
import org.crsh.term.spi.TermIO;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

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

  public String getProperty(String name) {
    ConnectionData data = conn.getConnectionData();
    if (data != null)
    {
      HashMap map = data.getEnvironment();
      if (map != null) {
        Object value = map.get(name);
        if (value != null) {
          return value.toString();
        }
      }
    }
    return null;
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

  public void write(CharSequence s) throws IOException {
    termIO.write(s.toString());
  }

  public void write(Style style) throws IOException {
    if (style == Style.reset) {
      termIO.resetAttributes();
      termIO.write("");
    } else {
      //
      if (style instanceof Style.Composite) {
        Style.Composite composite = (Style.Composite)style;
        if (composite.getBold() != null) {
          termIO.setBold(composite.getBold());
        }
        if (composite.getUnderline() != null) {
          termIO.setUnderlined(composite.getUnderline());
        }
        if (composite.getBlink() != null) {
          termIO.setBlink(composite.getBlink());
        }
        Color fg = composite.getForeground();
        if (fg != null) {
          if (fg == Color.def) {
            termIO.setForegroundColor(BasicTerminalIO.COLORINIT);
          } else {
            termIO.setForegroundColor(30 + fg.code);
          }
        }
        Color bg = composite.getBackground();
        if (bg != null) {
          if (bg == Color.def) {
            termIO.setBackgroundColor(BasicTerminalIO.COLORINIT);
          } else {
            termIO.setBackgroundColor(30 + bg.code);
          }
        }
      } else {
        termIO.resetAttributes();
      }
    }
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

  public void cls() throws IOException {
    termIO.eraseScreen();
    termIO.setCursor(0, 0);
  }
}
