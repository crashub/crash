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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTerm implements Term {

  /** . */
  private StringBuilder writer;

  /** . */
  private boolean closed;

  /** . */
  private BlockingDeque<TermEvent> actions;

  /** . */
  private final TermProcessor processor;

  /** . */
  private final Logger log = LoggerFactory.getLogger(TestTerm.class);

  /** . */
  private final Thread thread = new Thread() {
    @Override
    public void run() {
      final AtomicBoolean wantClose = new AtomicBoolean(false);
      while (!closed) {
        try {
          final TermEvent action = actions.takeFirst();
          final AtomicBoolean done = new AtomicBoolean(false);
          TermResponseContext ctx = new TermResponseContext() {
            public void setEcho(boolean echo) {
            }
            public TermEvent read() throws IOException {
              return TestTerm.this.read();
            }
            public void write(String msg) throws IOException {
              TestTerm.this.write(msg);
            }
            public void setPrompt(String prompt) {
            }
            public void done(boolean close) {
              done.set(true);
              if (close) {
                wantClose.set(true);
              }
            }
          };
          boolean consumed = processor.process(action, ctx);
          if (!consumed) {
            actions.addFirst(action);
          } else {
            while (!done.get()) {
              // Wait until it is processed
            }
          }
        } catch (Exception e) {
          log.error("Action delivery failed", e);
        }
      }

      //
      if (wantClose.get()) {
        close();
      }

    }
  };

  public TestTerm(TermProcessor processor) {
    this.writer = new StringBuilder();
    this.closed = false;
    this.actions = new LinkedBlockingDeque<TermEvent>();
    this.processor = processor;

    //
    thread.start();
  }

  public String getOutput() {
    String s = writer.toString();
    writer.setLength(0);
    return s;
  }

  public void add(TermEvent action) {
    if (action == null) {
      throw new NullPointerException();
    }
    actions.addLast(action);
  }

  public TermEvent read() throws IOException {
    if (closed) {
      throw new IllegalStateException();
    }
    try {
      return actions.takeFirst();
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  public void write(String data) throws IOException {
    if (closed) {
      throw new IllegalStateException();
    }
    writer.append(data);
  }

  public void setEcho(boolean echo) {
  }

  public void addToHistory(CharSequence line) {
  }

  public void close() {
    closed = true;
  }
}
