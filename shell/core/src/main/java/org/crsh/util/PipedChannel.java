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

package org.crsh.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;

public class PipedChannel {

  /** . */
  private final LinkedList<Integer> queue;

  /** . */
  private final Object lock;

  /** . */
  private boolean closed;

  /** . */
  private InputStream in;

  /** . */
  private OutputStream out;

  public PipedChannel() {
    this.queue = new LinkedList<Integer>();
    this.lock = new Object();
    this.closed = false;
    in = new InputStream();
    out = new OutputStream();
  }

  public InputStream getIn() {
    return in;
  }

  public OutputStream getOut() {
    return out;
  }

  class InputStream extends java.io.InputStream {
    @Override
    public int read() throws IOException {
      synchronized (lock) {
        while (true) {
          if (queue.size() > 0) {
            return queue.removeFirst();
          } else {
            if (closed) {
              throw new IOException("closed");
            } else {
              try {
                lock.wait();
              }
              catch (InterruptedException e) {
                InterruptedIOException iioe = new InterruptedIOException();
                iioe.initCause(e);
                throw iioe;
              }
            }
          }
        }
      }
    }

    @Override
    public void close() throws IOException {
      synchronized (lock) {
        if (!closed) {
          closed = true;
          lock.notifyAll();
        }
      }
    }
  }

  class OutputStream extends java.io.OutputStream {
    @Override
    public void write(int b) throws IOException {
      synchronized (lock) {
        if (closed) {
          throw new IOException("closed");
        }
        queue.add(b);
      }
    }

    @Override
    public void flush() throws IOException {
      synchronized (lock) {
        if (closed) {
          throw new IOException("closed");
        }
        lock.notifyAll();
      }
    }

    @Override
    public void close() throws IOException {
      synchronized (lock) {
        if (!closed) {
          closed = true;
          lock.notifyAll();
        }
      }
    }
  }
}
