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

package org.crsh.telnet.term;

import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;
import org.crsh.ProcessorListener;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.term.BaseTerm;
import org.crsh.shell.impl.CRaSH;
import org.crsh.Processor;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetHandler implements Shell {

  /** . */
  private BaseTerm term;

  /** . */
  private AsyncShell asyncShell;

  /** . */
  private CRaSH shell;

  /** . */
  private Processor processor;

  /** . */
  private TelnetIO io;

  public void run(Connection conn) {

    //
    shell = TelnetLifeCycle.instance.getShellFactory().build();
    asyncShell = new AsyncShell(TelnetLifeCycle.instance.getExecutor(), shell);
    io = new TelnetIO(conn);
    term = new BaseTerm(io);
    processor = new Processor(term, asyncShell);

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        io.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        asyncShell.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        shell.close();
      }
    });

    //
    processor.run();
  }

  public void connectionIdle(ConnectionEvent connectionEvent) {
  }

  public void connectionTimedOut(ConnectionEvent connectionEvent) {
    System.out.println("connectionTimedOut");
  }

  public void connectionLogoutRequest(ConnectionEvent connectionEvent) {
    System.out.println("logoutRequest");
  }

  public void connectionSentBreak(ConnectionEvent connectionEvent) {
  }

  public static Shell createShell() {
    return new TelnetHandler();
  }
}
