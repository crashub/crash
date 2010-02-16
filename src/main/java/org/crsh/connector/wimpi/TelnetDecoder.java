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

package org.crsh.connector.wimpi;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import org.crsh.connector.ShellConnector;
import org.crsh.util.Input;
import org.crsh.util.InputDecoder;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetDecoder extends InputDecoder {

  /** . */
  private final ShellConnector connector;

  /** . */
  private final BasicTerminalIO termIO;

  public TelnetDecoder(ShellConnector connector, BasicTerminalIO termIO) {
    this.connector = connector;
    this.termIO = termIO;
  }

  public void append(int i) throws IOException {
    if (i == TerminalIO.DELETE) {
      appendDel();
    } else if (i == 10) {
      appendData('\r');
      appendData('\n');
    } else if (i >= 0 && i < 128) {
      if (i == 3) {
        
      } else {
        appendData((char)i);
      }
    } else {
      // log
    }

    //
    if (hasNext()) {
      Input input = next();
      if (input instanceof Input.Chars) {
        String line = ((Input.Chars)input).getValue();
        String resp = connector.evaluate(line);
        termIO.write(resp);
        termIO.flush();
      }
    }
  }

  @Override
  protected void echoDel() throws IOException {
    termIO.moveLeft(1);
    termIO.write(' ');
    termIO.moveLeft(1);
    termIO.flush();
  }

  @Override
  protected void echo(String s) throws IOException {
    termIO.write(s);
    termIO.flush();
  }

  @Override
  protected void echo(char c) throws IOException {
    termIO.write(c);
    termIO.flush();
  }
}
