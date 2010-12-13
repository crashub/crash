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

package org.crsh.term.processor;

import org.crsh.term.Term;
import org.crsh.term.TermEvent;
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
  final Term term;

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
      request.handle(event);

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

    public void handle(TermEvent event) {

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
