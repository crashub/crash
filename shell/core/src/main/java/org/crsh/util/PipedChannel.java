package org.crsh.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;

/**
 * A combination of an {@link InputStream} and an {@link OutputStream}, simpler than what java provides
 * and more suitable for unit testing. This class is not optimized for performance.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
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
