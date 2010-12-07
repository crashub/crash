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

import org.crsh.console.ClientOutput;
import org.crsh.console.Console;
import org.crsh.term.processor.TermProcessor;
import org.crsh.term.processor.TermResponseContext;
import org.crsh.term.spi.TermIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseTerm implements Term, Runnable {

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
  private final Logger log = LoggerFactory.getLogger(BaseTerm.class);

  /** . */
  private final TermProcessor processor;

  /** . */
  private final AtomicInteger status;

  /** . */
  private final Object lock = new Object();

  /** . */
  private final LinkedList<Awaiter> awaiters = new LinkedList<Awaiter>();

  /** . */
  private final LinkedList<CharSequence> history;

  /** . */
  private CharSequence historyBuffer;

  /** . */
  private int historyCursor;

  /** . */
  private String prompt;

  /** . */
  private final TermIO io;

  /** . */
  private final Console console;

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

  public BaseTerm(TermIO io) {
    this(io, null);
  }

  public BaseTerm(final TermIO io, TermProcessor processor) {
    this.processor = processor;
    this.status = new AtomicInteger(STATUS_INITIAL);
    this.history = new LinkedList<CharSequence>();
    this.historyBuffer = null;
    this.historyCursor = -1;
    this.io = io;
    this.console = new Console(new ClientOutput() {
      @Override
      protected void writeCRLF() throws IOException {
        io.writeCRLF();
        io.flush();
      }

      @Override
      protected void write(CharSequence s) throws IOException {
        io.write(s.toString());
        io.flush();
      }

      @Override
      protected void write(char c) throws IOException {
        io.write(c);
        io.flush();
      }

      @Override
      protected void writeDel() throws IOException {
        io.writeDel();
        io.flush();
      }

      @Override
      protected boolean writeMoveLeft() throws IOException {
        return io.moveLeft();
      }

      @Override
      protected boolean writeMoveRight() throws IOException {
        return io.moveRight();
      }
    });
  }

  public void run() {

    //
    if (!status.compareAndSet(STATUS_INITIAL, STATUS_OPEN)) {
      throw new IllegalStateException();
    }

    //
    TermAction action = new TermAction.Init();

    //
    while (status.get() == STATUS_OPEN) {
      if (action == null) {
        try {
          action = _read();
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
      final TermAction action2 = action;
      action = null;

      //
      if (awaiter != null) {
        awaiter.give(action2);
      } else {

        //
        TermResponseContext ctx = new TermResponseContext() {

          public void setEcho(boolean echo) {
            console.setEchoing(echo);
          }
          public TermAction read() throws IOException {
            return BaseTerm.this.read();
          }
          public void write(String msg) throws IOException {
            BaseTerm.this.write(msg);
          }

          public void setPrompt(String prompt) {
            BaseTerm.this.prompt = prompt;
          }

          public void done(boolean close) {
            try {
              String p = prompt == null ? "% " : prompt;
              console.getWriter().write("\r\n");
              console.getWriter().write(p);
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
                io.close();
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
        } else if (action2 instanceof TermAction.ReadLine) {
          CharSequence line = ((TermAction.ReadLine)action2).getLine();
          historyCursor = -1;
          historyBuffer = null;
          if (line.length() > 0) {
            history.addFirst(((TermAction.ReadLine)action2).getLine());
          }
        }
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
    TermAction taken = awaiter.take();
    return taken;
  }

  public void close() {

    //
    status.compareAndSet(STATUS_OPEN, STATUS_WANT_CLOSE);

    //
    if (status.compareAndSet(STATUS_WANT_CLOSE, STATUS_CLOSING)) {
      try {
        log.debug("Closing connection");
        io.flush();
        io.close();
      } catch (IOException e) {
        log.debug("Exception thrown during term close()", e);
      } finally {
        status.set(STATUS_CLOSED);
      }
    }
  }

  public TermAction _read() throws IOException {

    while (true) {
      int code = io.read();
      CodeType type = io.decode(code);
      switch (type) {
        case DELETE:
          console.getClientInput().del();
          break;
        case UP:
        case DOWN:
          int nextHistoryCursor = historyCursor +  (type == CodeType.UP ? + 1 : -1);
          if (nextHistoryCursor >= -1 && nextHistoryCursor < history.size()) {
            CharSequence s = nextHistoryCursor == -1 ? historyBuffer : history.get(nextHistoryCursor);
            CharSequence t = console.getClientInput().replace(s);
            if (historyCursor == -1) {
              historyBuffer = t;
            }
            if (nextHistoryCursor == -1) {
              historyBuffer = null;
            }
            historyCursor = nextHistoryCursor;
          }
          break;
        case RIGHT:
          console.getClientInput().moveRight();
          break;
        case LEFT:
          console.getClientInput().moveLeft();
          break;
        case BREAK:
          log.debug("Want to cancel evaluation");
          console.clearBuffer();
          return new TermAction.CancelEvaluation();
        case CHAR:
          if (code >= 0 && code < 128) {
            console.getClientInput().write((char)code);
          } else {
            log.debug("Unhandled char " + code);
          }
          break;
      }

      //
      if (console.getReader().hasNext()) {
        CharSequence input = console.getReader().next();
        return new TermAction.ReadLine(input);
      }
    }
  }

  public void write(String msg) throws IOException {
    console.getWriter().write(msg);
  }
}