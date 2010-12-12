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

package org.crsh.term;

import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.term.processor.TermProcessor;
import org.crsh.term.processor.TermResponseContext;
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
  private final AsyncShell shell;

  /** . */
  private volatile TermStatus status;

  public TermShellAdapter2(AsyncShell connector) {
    this.shell = connector;
    this.status = TermStatus.READY;
  }

  public String getPrompt() {
    return shell.getPrompt();
  }

  public TermStatus getStatus() {
    return status;
  }

  public void close() {
    // No nop for now
  }

  public boolean process(TermAction action, TermResponseContext responseContext) {
    try {
      return _process(action, responseContext);
    } catch (IOException e) {
      e.printStackTrace();
      return true;
    }
  }

  private boolean _process(TermAction action, final TermResponseContext responseContext) throws IOException {

    // Take care of that here
    if (action instanceof TermAction.Init) {
      String welcome = shell.open();
      responseContext.write(welcome);
      return true;
    }

    //
    boolean processed;

    //
    switch (status) {
      case READY:
        if (action instanceof TermAction.ReadLine) {
          String line = ((TermAction.ReadLine)action).getLine().toString();
          status = TermStatus.PROCESSING;
          log.debug("Submitting command " + line);

          shell.evaluate(line, new ShellResponseContext() {
            public String readLine(String msg, boolean echo) {
              try {
                status = TermStatus.READING_INPUT;
                responseContext.setEcho(echo);
                responseContext.write(msg);
                TermAction action = responseContext.read();
                CharSequence line = null;
                if (action instanceof TermAction.ReadLine) {
                  line = ((TermAction.ReadLine) action).getLine();
                  log.debug("Read from console");
                } else {
                  log.debug("Ignoring action " + action + " returning null");
                }
                responseContext.write("\r\n");
                return line.toString();
              } catch (Exception e) {
                log.error("Reading from console failed", e);
                return null;
              } finally {
                responseContext.setEcho(true);
                status = TermStatus.PROCESSING;
              }
            }

            public void done(ShellResponse response) {

              // Update prompt first
              String prompt = shell.getPrompt();
              responseContext.setPrompt(prompt);

              //
              if (response instanceof ShellResponse.Close) {
                status = TermStatus.SHUTDOWN;
                responseContext.done(true);
              } else {
                if (response instanceof ShellResponse.Cancelled) {
//                  responseContext.done(false);
                } else {
                  String ret = response.getText();
                  log.debug("Command completed with result " + ret);
                  try {
                    responseContext.write(ret);
                  } catch (IOException e) {
                    log.error("Write to term failure", e);
                  }
                  responseContext.done(false);
                }
                status = TermStatus.READY;
              }



            }
          });
        } else if (action instanceof TermAction.CancelEvaluation) {
          responseContext.done(false);
        }
        processed = true;
        break;
      case READING_INPUT:
        processed = false;
        break;
      case PROCESSING:
        if (action instanceof TermAction.CancelEvaluation) {
          if (shell.cancel()) {
            log.debug("Evaluation cancelled");
            responseContext.done(false);
          // We signal it's done here but the command may still be processing
          } else {
            log.debug("Attempt to cancel evaluation failed");
          }
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