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

package org.crsh.connector.telnet;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.net.Connection;
import org.crsh.connector.Term;
import org.crsh.connector.TermAction;
import org.crsh.connector.TermProcessor;
import org.crsh.util.Input;
import org.crsh.util.InputDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetTerm extends InputDecoder implements Term {

  /** . */
  private final Logger log = LoggerFactory.getLogger(TelnetTerm.class);

  /** . */
  private final Connection conn;

  /** . */
  private final BasicTerminalIO termIO;

  /** . */
  private final TermProcessor processor;

  /** . */
  private boolean closed;

  /** . */
  private final Object lock = new Object();

  /** . */
  private final LinkedList<Awaiter> awaiters = new LinkedList<Awaiter>();

  private static class Awaiter {

    TermAction action;

    private Awaiter() {
    }

    public synchronized void give(TermAction action) {
      this.action = action;
      notify();
    }

    public synchronized TermAction take() {
      try {
        wait();
        return action;
      } catch (InterruptedException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  public TelnetTerm(Connection conn) {
    this.conn = conn;
    this.termIO = conn.getTerminalIO();
    this.processor = null;
    this.closed = false;
  }

  public TelnetTerm(Connection conn, TermProcessor processor) {
    this.conn = conn;
    this.termIO = conn.getTerminalIO();
    this.processor = processor;
    this.closed = false;
  }

  public void run() {

    //
    TermAction action = null;

    //
    while (!closed) {
      try {
        if (action == null) {
          action = _read();
        }

        //
        Awaiter awaiter = null;
        synchronized (lock) {
          if (awaiters.size() > 0) {
            awaiter = awaiters.removeFirst();
          }
        }

        // Consume
        TermAction action2 = action;
        action = null;

        //
        if (awaiter != null) {
          awaiter.give(action2);
        } else {
          boolean processed = processor.process(TelnetTerm.this, action2);
          if (!processed) {
            // Push back
            action = action2;
          }
        }
      } catch (Exception e) {
        log.error("Action delivery failed", e);
      }
    }
  }

  public TermAction read() throws IOException {
    Awaiter awaiter;

    //
    synchronized (lock) {
      awaiter = new Awaiter();
      awaiters.add(awaiter);
    }

    //
    return awaiter.take();
  }

  public void close() {
    closed = true;
    try {
      termIO.flush();
      conn.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public TermAction _read() throws IOException {

    while (true) {
      int code = termIO.read();

      if (code == TerminalIO.DELETE) {
        appendDel();
      } else if (code == 10) {
        appendData('\r');
        appendData('\n');
      } else if (code >= 0 && code < 128) {
        if (code == 3) {
          log.debug("Want to cancel evaluation");
          return new TermAction.CancelEvaluation();
        } else {
          appendData((char)code);
        }
      } else {
        log.debug("Unhandled char " + code);
      }

      if (hasNext()) {
        Input input = next();
        if (input instanceof Input.Chars) {
          return new TermAction.ReadLine(((Input.Chars)input).getValue());
        } else {
          throw new UnsupportedOperationException();
        }
      }
    }
  }

  public void write(String prompt) throws IOException {
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
    write(s);
  }

  @Override
  protected void echo(char c) throws IOException {
    termIO.write(c);
    termIO.flush();
  }
}
