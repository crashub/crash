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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
  private final AtomicInteger status;

  /** . */
  private final BlockingQueue<EventRequest> requestQueue;

  public Processor(Term term, TermProcessor processor) {
    this.term = term;
    this.processor = processor;
    this.status = new AtomicInteger(STATUS_INITIAL);
    this.requestQueue = new LinkedBlockingQueue<EventRequest>();
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
    requestQueue.add(new ShellInvoker());

    //
    while (status.get() == STATUS_OPEN) {

      //
      System.out.println("About to take request from queue to deliver event " + event);
      EventRequest request = requestQueue.take();
      System.out.println("Found request " + request + " in queue");

      //
      request.give(event);

      //
      try {
        event = term.read();
        log.debug("read term data " + event);
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

  private class ShellInvoker implements EventRequest {

    public void give(TermEvent event) {

      TermResponseContext ctx = new TermResponseContext() {

        /** . */
        private String prompt;

        public void setEcho(boolean echo) {
          term.setEcho(echo);
        }
        public TermEvent read() throws IOException {

          // Go in the queue and wait for an event
          final CountDownLatch latch = new CountDownLatch(1);
          final AtomicReference<TermEvent> eventRef = new AtomicReference<TermEvent>();

          System.out.println("Adding event request callback to the queue");
          requestQueue.add(new EventRequest() {
            public void give(TermEvent event) {
              eventRef.set(event);
              latch.countDown();
            }
          });

          //
          System.out.println("About to wait delivery of event");
          try {
            latch.await();
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }

          //
          System.out.println("Got event " + eventRef.get());
          return eventRef.get();
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
          System.out.println("Adding new shell invoker to the queue as work is done");
          requestQueue.add(new ShellInvoker());

          //
          if (close) {
            // If we succeded we close the term
            // It will cause an exception to be thrown for the thread that are waiting in the
            // blocking read operation
            close();
          }
        }
      };

      // Process
      processor.process(event, ctx);

      // Maybe this should not be placed here
      if (event instanceof TermEvent.ReadLine) {
        CharSequence line = ((TermEvent.ReadLine)event).getLine();
        if (line.length() > 0) {
          term.addToHistory(line);
        }
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

  private interface EventRequest {

    void give(TermEvent event);

  }

}
