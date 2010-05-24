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
import org.crsh.shell.ShellConnector;
import org.crsh.util.CompletionHandler;
import org.crsh.util.Input;
import org.crsh.util.InputDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetDecoder extends InputDecoder {

  /** . */
  private final Logger log = LoggerFactory.getLogger(TelnetDecoder.class);

  /** . */
  private final Connection conn;

  /** . */
  private final ShellConnector connector;

  /** . */
  private final BasicTerminalIO termIO;

  /** . */
  private TelnetStatus status;

  public TelnetDecoder(Connection connection) {
    this.conn = connection;
    this.connector = new ShellConnector(TelnetLifeCycle.instance.getShellBuilder().build());
    this.termIO = connection.getTerminalIO();
    this.status = TelnetStatus.SHUTDOWN;
  }

  public void run() throws IOException {
    String welcome = connector.open();
    writeFully(welcome);

    // Go to ready state
    this.status = TelnetStatus.READY;

    //
    while (status != TelnetStatus.SHUTDOWN) {
      int code = termIO.read();
      append(code);
    }
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
        log.debug("Want to cancel evaluation");
        if (connector.cancelEvalutation()) {
          log.debug("Evaluation cancelled");
        }
        String s = "\r\n" + connector.getPrompt();
        writeFully(s);
      } else {
        appendData((char)i);
      }
    } else {
      log.debug("Unhandled char " + i);
    }

    //
    if (hasNext()) {
      Input input = next();
      if (input instanceof Input.Chars) {
        String line = ((Input.Chars)input).getValue();

        switch (status)
        {
          case READY:
            log.debug("Submitting command " + line);
            connector.submitEvaluation(line, new CompletionHandler<String>() {
              public void completed(String s) {
                log.debug("Command completed with result " + s);
                try {
                  writeFully(s);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
            break;
          case READING_INPUT:
            log.info("Submitting back input");
            break;
          case SHUTDOWN:
            throw new AssertionError("Does not make sense");
        }
      }
    }

    // Update status
    if (connector.isClosed()) {
      status = TelnetStatus.SHUTDOWN;
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
