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

package org.crsh.term;

import org.crsh.term.processor.TermProcessor;
import org.crsh.term.processor.TermResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
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
  private final TermProcessor processor;

  /** . */
  private final Object lock = new Object();

  /** . */
  private final AtomicInteger status;

  /** . */
  private final LinkedList<Awaiter> awaiters = new LinkedList<Awaiter>();

  public Processor(Term term, TermProcessor processor) {
    this.term = term;
    this.processor = processor;
    this.status = new AtomicInteger(STATUS_INITIAL);
  }

  public void run() {

    //
    if (!status.compareAndSet(STATUS_INITIAL, STATUS_OPEN)) {
      throw new IllegalStateException();
    }

    //
    TermEvent action = new TermEvent.Init();

    //
    while (status.get() == STATUS_OPEN) {
      if (action == null) {
        try {
          action = term.read();
          log.debug("read term data " + action);
        } catch (IOException e) {
          if (status.get() == STATUS_OPEN) {
            log.error("Could not read term data", e);
          } else {
            log.debug("Exception but term is considered as closed", e);
            // We continue it will lead to getting out of the loop
            continue;
          }
        }
      }

      //
      Awaiter awaiter = null;
      synchronized (lock) {
        if (awaiters.size() > 0) {
          awaiter = awaiters.removeFirst();
        }
      }

      // Consume
      final TermEvent action2 = action;
      action = null;

      //
      if (awaiter != null) {
        awaiter.give(action2);
      } else {

        //
        TermResponseContext ctx = new TermResponseContext() {

          /** . */
          private String prompt;

          public void setEcho(boolean echo) {
            term.setEcho(echo);
          }
          public TermEvent read() throws IOException {

            // Go in the queue and wait for an event
            Awaiter awaiter;
            synchronized (lock) {
              awaiter = new Awaiter();
              awaiters.add(awaiter);
            }

            // Returns the event
            return awaiter.take();
          }
          public void write(String msg) throws IOException {
            term.write(msg);
          }

          public void setPrompt(String prompt) {
            this.prompt = prompt;
          }

          public void done(boolean close) {
            try {
              String p = prompt == null ? "% " : prompt;
              term.write("\r\n");
              term.write(p);
            } catch (IOException e) {
              e.printStackTrace();
            }

            //
            if (close) {
              // Change status
              if (status.compareAndSet(STATUS_OPEN, STATUS_WANT_CLOSE)) {
                // If we succeded we close the term
                // It will cause an exception to be thrown for the thread that are waiting in the
                // blocking read operation
                // io.close();
              }
            }
          }
        };

        //
        boolean processed = processor.process(action2, ctx);
        if (!processed) {
          // Push back
          log.debug("Pushing back action " + action2);
          action = action2;
        } else if (action2 instanceof TermEvent.ReadLine) {
          CharSequence line = ((TermEvent.ReadLine)action2).getLine();
          if (line.length() > 0) {
            term.addToHistory(((TermEvent.ReadLine)action2).getLine());
          }
        }
      }
    }
  }

  private static class Awaiter {

    TermEvent action;

    private Awaiter() {
    }

    public synchronized void give(TermEvent action) {
      this.action = action;
      notify();
    }

    public synchronized TermEvent take() {
      try {
        wait();
        return action;
      } catch (InterruptedException e) {
        e.printStackTrace();
        return null;
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
