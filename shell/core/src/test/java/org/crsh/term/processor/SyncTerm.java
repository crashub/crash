package org.crsh.term.processor;

import org.crsh.AbstractTestCase;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
    return 50;
  }

  public String getProperty(String name) {
    return null;
  }

  public void setEcho(boolean echo) {
  
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

  public void write(CharSequence msg) throws IOException {
  
  }

  public Appendable getInsertBuffer() {
    return null;
  }

  public CharSequence getBuffer() {
    return null;
  }

  public void addToHistory(CharSequence line) {
  
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
