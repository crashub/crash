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

package org.crsh.connector;

import org.crsh.shell.Connector;
import org.crsh.shell.ConnectorResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TermShellAdapter {

  /** . */
  private final Logger log = LoggerFactory.getLogger(TermShellAdapter.class);

  /** . */
  private final Connector connector;

  /** . */
  private TermStatus status;

  /** . */
  private final Term term;

  public TermShellAdapter(Term term, Connector connector) {
    this.connector = connector;
    this.status = TermStatus.SHUTDOWN;
    this.term = term;
  }

  public TermStatus getStatus() {
    return status;
  }

  public void close() {
    term.close();
    try {
      connector.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() throws IOException {
    String welcome = connector.open();
    term.write(welcome);

    // Go to ready state
    this.status = TermStatus.READY;

    //
    while (status != TermStatus.SHUTDOWN) {

      //
      TermAction tmp = term.read();

      if (tmp instanceof TermAction.ReadLine) {

        //
        String line = ((TermAction.ReadLine)tmp).getLine();

        //
        switch (status)
        {
          case READY:
            log.debug("Submitting command " + line);
            connector.submitEvaluation(line, new ConnectorResponseContext() {
              public void completed(String s) {
                log.debug("Command completed with result " + s);
                try {
                  term.write(s);
                } catch (IOException e) {
                  e.printStackTrace();
                } finally {
                  status = TermStatus.READY;
                }
              }
              public String readLine(String s) {
                try {
                  status = TermStatus.READING_INPUT;
                  return "FOOOOOOOOO";
                } finally {
                  status = TermStatus.PROCESSING;
                }
              }
            });
            break;
          case READING_INPUT:
            log.info("Submitting back input");
            break;
          case PROCESSING:
            log.info("System is already processing a command");
            break;
          case SHUTDOWN:
            throw new AssertionError("Does not make sense");
        }
      } else if (tmp instanceof TermAction.CancelEvaluation) {
        if (connector.cancelEvalutation()) {
          log.debug("Evaluation cancelled");
        }
        String s = "\r\n" + connector.getPrompt();
        term.write(s);

        // Maybe should clear buffer ?
      }


      //
      if (connector.isClosed()) {
        status = TermStatus.SHUTDOWN;
      }
    }
  }
}
