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
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;
import org.crsh.connector.ShellConnector;
import org.crsh.util.Input;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetHandler implements Shell {

  /** . */
  private Connection conn;

  /** . */
  private BasicTerminalIO termIO;

  public void run(Connection connection) {

    this.conn = connection;
    this.termIO = connection.getTerminalIO();

    //
    ShellConnector connector = new ShellConnector(TelnetLifeCycle.instance.getShellBuilder());

    conn.addConnectionListener(this);

    String welcome = connector.open();

    TelnetDecoder echo = new TelnetDecoder(connector, termIO);

    try {
      termIO.write(welcome);
      termIO.flush();

      while (true) {
        int code = termIO.read();
        echo.append(code);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void connectionIdle(ConnectionEvent connectionEvent) {
    try {
      termIO.write("CONNECTION_IDLE");
      termIO.flush();
    } catch (IOException e) {
      // log.error("connectionIdle()", e);
    }
  }

  public void connectionTimedOut(ConnectionEvent connectionEvent) {
    try {
      termIO.write("CONNECTION_TIMEDOUT");
      termIO.flush();
      conn.close();
    } catch (Exception e) {
//      log.error("connectionTimedOut()", ex);
    }
  }

  public void connectionLogoutRequest(ConnectionEvent connectionEvent) {
    try {
      termIO.write("CONNECTION_LOGOUTREQUEST");
      termIO.flush();
      conn.close();
    } catch (Exception e) {
//      log.error("connectionLogoutRequest()", ex);
    }
  }

  public void connectionSentBreak(ConnectionEvent connectionEvent) {
    try {
      termIO.write("CONNECTION_BREAK");
      termIO.flush();
    } catch (Exception e) {
      // log.error("connectionSentBreak()", ex);
    }
  }

  public static Shell createShell() {
    return new TelnetHandler();
  }
}
