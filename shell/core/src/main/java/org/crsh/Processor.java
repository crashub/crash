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

package org.crsh;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Processor implements Runnable {

  /** . */
  private final Logger log = LoggerFactory.getLogger(Processor.class);

  /** . */
  private static final int STATUS_INITIAL = 0;

  /** . */
  private static final int STATUS_OPEN = 1;

  /** . */
  private static final int STATUS_CLOSED = 2;

  /** . */
  private static final int STATUS_WANT_CLOSE = 3;

  /** . */
  private static final int STATUS_CLOSING = 4;

  /** . */
  private final Term term;

  /** . */
  private final AtomicInteger status;

  /** . */
  private final Shell shell;

  /** . */
  private volatile ShellProcess process;

  public Processor(Term term, Shell shell) {
    this.term = term;
    this.status = new AtomicInteger(STATUS_INITIAL);
    this.shell = shell;
    this.process = null;
  }

  public void run() {
    try {
      _run();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void _run() throws InterruptedException {

    //
    if (!status.compareAndSet(STATUS_INITIAL, STATUS_OPEN)) {
      throw new IllegalStateException();
    }

    //
    TermEvent event = new TermEvent.Init();

    //
    while (status.get() == STATUS_OPEN) {

      if (event instanceof TermEvent.Init) {
        try {
          String welcome = shell.getWelcome();
          log.debug("Writing welcome message to term");
          term.write(welcome);
          log.debug("Wrote welcome message to term");
          writePrompt();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      } else if (event instanceof TermEvent.ReadLine) {
        String line = ((TermEvent.ReadLine)event).getLine().toString();
        log.debug("Submitting command " + line);

        //
        ShellInvoker invoker = new ShellInvoker();

        // Process
        shell.process(((TermEvent.ReadLine)event).getLine().toString(), invoker);

        if (line.length() > 0) {
          term.addToHistory(line);
        }
      } else if (event instanceof TermEvent.Break) {
        if (process != null) {
          process.cancel();
        } else {
          log.debug("Ignoring action " + event);
          writePrompt();
        }
      } else if (event instanceof TermEvent.Complete) {
        TermEvent.Complete complete = (TermEvent.Complete)event;
        String prefix = complete.getLine().toString();
        log.debug("About to get completions for " + prefix);
        List<String> completion = shell.complete(prefix);
        log.debug("Completions for " + prefix + " are " + completion);
        if (completion.size() >= 1) {

          // Try to find a common prefix

          try {
            term.write(completion.get(0));
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      //
      try {
        log.debug("About to read next term event");
        event = term.read();
        log.debug("Read next term event " + event);
      } catch (IOException e) {
        if (status.get() == STATUS_OPEN) {
          log.error("Could not read term data", e);
        } else {
          log.debug("Exception but term is considered as closed", e);
          // We continue it will lead to getting out of the loop
        }
      }
    }
  }

  private void writePrompt() {
    String prompt = shell.getPrompt();
    try {
      String p = prompt == null ? "% " : prompt;
      term.write("\r\n");
      term.write(p);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class ShellInvoker implements ShellProcessContext {

    public void begin(ShellProcess process) {
      Processor.this.process = process;
    }

    public String readLine(String msg, boolean echo) {
      try {
        term.setEcho(echo);
        term.write(msg);
        TermEvent action = term.read();
        CharSequence line = null;
        if (action instanceof TermEvent.ReadLine) {
          line = ((TermEvent.ReadLine)action).getLine();
          log.debug("Read from console");
        }
        else {
          log.debug("Ignoring action " + action + " returning null");
        }
        term.write("\r\n");
        return line.toString();
      }
      catch (Exception e) {
        log.error("Reading from console failed", e);
        return null;
      }
      finally {
        term.setEcho(true);
      }
    }

    public void end(ShellResponse response) {
      try {

        //
        if (response instanceof ShellResponse.Close) {
          // If we succeded we close the term
          // It will cause an exception to be thrown for the thread that are waiting in the
          // blocking read operation
          close();
        }
        else {
          if (response instanceof ShellResponse.Cancelled) {
          }
          else {
            String ret = response.getText();
            log.debug("Command completed with result " + ret);
            try {
              term.write(ret);
            }
            catch (IOException e) {
              log.error("Write to term failure", e);
            }
            process = null;
          }
        }

        //
        writePrompt();
      }
      finally {
        process = null;
      }
    }
  }

  public void close() {

    //
    status.compareAndSet(STATUS_OPEN, STATUS_WANT_CLOSE);

    //
    if (status.compareAndSet(STATUS_WANT_CLOSE, STATUS_CLOSING)) {
      try {
        term.close();
      } finally {
        status.set(STATUS_CLOSED);
      }
    }
  }
}