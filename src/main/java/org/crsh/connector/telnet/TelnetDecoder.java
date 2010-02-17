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

package org.crsh.connector.telnet;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import org.crsh.connector.ShellConnector;
import org.crsh.util.CompletionHandler;
import org.crsh.util.Input;
import org.crsh.util.InputDecoder;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetDecoder extends InputDecoder {

  /** . */
  private final Connection conn;

  /** . */
  private final ShellConnector connector;

  /** . */
  private final BasicTerminalIO termIO;

  public TelnetDecoder(Connection connection) {
    this.conn = connection;
    this.connector = new ShellConnector(TelnetLifeCycle.instance.getShellBuilder());
    this.termIO = connection.getTerminalIO();
  }

  public void run() throws IOException {
    String welcome = connector.open();
    writeFully(welcome);

    //
    while (!isClosed()) {
      int code = termIO.read();
      append(code);
    }
  }

  public boolean isClosed() {
    return connector.isClosed();
  }

  public void close() {
    try {
      termIO.flush();
      conn.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      connector.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void append(int i) throws IOException {
    if (i == TerminalIO.DELETE) {
      appendDel();
    } else if (i == 10) {
      appendData('\r');
      appendData('\n');
    } else if (i >= 0 && i < 128) {
      if (i == 3) {
        System.out.println("Wanting to cancel evaluation!!!!!!!!!!!");
        if (connector.cancelEvalutation()) {
          System.out.println("Evaluation cancelled");
        }
        String s = "\r\n" + connector.getPrompt();
        writeFully(s);
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
        System.out.println("Submitting command " + line);
        connector.submitEvaluation(line, new CompletionHandler<String>() {
          public void completed(String s) {
            System.out.println("Command completed with result " + s);
            try {
              writeFully(s);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
      }
    }
  }

  private void writeFully(String prompt) throws IOException {
    termIO.write(prompt);
    termIO.flush();
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
    writeFully(s);
  }

  @Override
  protected void echo(char c) throws IOException {
    termIO.write(c);
    termIO.flush();
  }
}
