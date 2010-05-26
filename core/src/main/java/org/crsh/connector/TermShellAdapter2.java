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
public class TermShellAdapter2 implements TermProcessor {

  /** . */
  private final Logger log = LoggerFactory.getLogger(TermShellAdapter2.class);

  /** . */
  private final Connector connector;

  /** . */
  private volatile TermStatus status;

  public TermShellAdapter2(Connector connector) {
    this.connector = connector;
    this.status = TermStatus.READY;
  }

  public TermStatus getStatus() {
    return status;
  }

/*
  public void close() {
    term.close();
    try {
      connector.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
*/

  private volatile boolean first = true;

  public boolean process(final Term term, TermAction action) {
    try {
      return _process(term, action);
    } catch (IOException e) {
      e.printStackTrace();
      return true;
    }
  }

  private boolean _process(final Term term, TermAction action) throws IOException {

    //
    if (first) {
      first = false;
      String welcome = connector.open();
      term.write(welcome);
    }

    //
    boolean processed;

    //
    switch (status) {
      case READY:
        if (action instanceof TermAction.ReadLine) {
          String line = ((TermAction.ReadLine)action).getLine();
          log.debug("Submitting command " + line);
          connector.submitEvaluation(line, new ConnectorResponseContext() {
            public void completed(String s) {
              log.debug("Command completed with result " + s);
              try {
                term.write(s);
              } catch (IOException e) {
                log.error("Write to term failure", e);
              } finally {
                status = TermStatus.READY;
              }
            }
            public String readLine(String s) {
              try {
                status = TermStatus.READING_INPUT;
                TermAction action = term.read();
                if (action instanceof TermAction.ReadLine) {
                  String line = ((TermAction.ReadLine) action).getLine();
                  log.debug("Read from console " + line);
                  return line;
                } else {
                  log.debug("Ignoring action " + action + " returning null");
                  return null;
                }
              } catch (Exception e) {
                log.error("Reading from console failed", e);
                return null;
              } finally {
                status = TermStatus.PROCESSING;
              }
            }
            public void close() {
              status = TermStatus.SHUTDOWN;
              term.close();
            }
          });
        } else {
          log.debug("Ignoring action " + action);
        }
        processed = true;
        break;
      case READING_INPUT:
        processed = false;
        break;
      case PROCESSING:
        if (action instanceof TermAction.CancelEvaluation) {
          if (connector.cancelEvalutation()) {
            log.debug("Evaluation cancelled");
          } else {
            log.debug("Attempt to cancel evaluation failed");
          }
          String s = "\r\n" + connector.getPrompt();
          term.write(s);
          // Maybe should clear char buffer ?
        } else {
          log.debug("Ignoring action " + action);
        }
        processed = true;
        break;
      case SHUTDOWN:
        throw new AssertionError("maybe todo");
      default:
        throw new AssertionError();
    }

    //
    return processed;
  }
}