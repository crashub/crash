/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.crsh.processor.term;

import org.crsh.AbstractTestCase;
import org.crsh.text.Chunk;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import java.util.concurrent.Callable;

public class SyncTerm implements Term {


  /** . */
  private final LinkedList<Throwable> failures;

  /** . */
  private final LinkedList<Callable<TermEvent>> queue;

  /** . */
  private final Object lock;

  /** . */
  private boolean closed;

  public SyncTerm() {
    this.failures = new LinkedList<Throwable>();
    this.queue = new LinkedList<Callable<TermEvent>>();
    this.lock = new Object();
    this.closed = false;
  }

  public void publish(final TermEvent event) {
    publish(new Callable<TermEvent>() {
      public TermEvent call() throws Exception {
        return event;
      }
    });
  }

  public void publish(final Callable<TermEvent> event) {
    synchronized (lock) {
      if (closed) {
        throw AbstractTestCase.failure("closed");
      }
      queue.addLast(event);
      lock.notifyAll();
    }
  }

  public int getWidth() {
    return 32;
  }

  public int getHeight() {
    return 40;
  }

  public String getProperty(String name) {
    return null;
  }

  public void setEcho(boolean echo) {
  }

  public boolean takeAlternateBuffer() throws IOException {
    return false;
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return false;
  }

  public TermEvent read() throws IOException {
    synchronized (lock) {
      while (true) {
        if (closed) {
          return TermEvent.close();
        } else if (queue.size() > 0) {
          try {
            Callable<TermEvent> callable = queue.removeFirst();
            return callable.call();
          }
          catch (Exception e) {
            if (e instanceof IOException) {
              throw (IOException)e;
            } else {
              throw new UndeclaredThrowableException(e);
            }
          }
        } else {
          try {
            lock.wait();
          }
          catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
          }
        }
      }
    }
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public void provide(Chunk element) throws IOException {
  }

  public Appendable getDirectBuffer() {
    return null;
  }

  public CharSequence getBuffer() {
    return null;
  }

  public void addToHistory(CharSequence line) {
  }

  public void flush() {
  }

  public void close() {
    synchronized (lock) {
      if (!closed) {
        closed = true;
        lock.notifyAll();
      }
    }
  }
}
