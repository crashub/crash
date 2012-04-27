package org.crsh.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class CloseableList implements Closeable {

  /** . */
  final Logger log = LoggerFactory.getLogger(CloseableList.class);

  /** . */
  private final ArrayList<Closeable> closeables;

  /** . */
  private final AtomicBoolean closed;

  public CloseableList() {
    this.closeables = new ArrayList<Closeable>();
    this.closed = new AtomicBoolean(false);
  }

  public boolean isClosed() {
    return closed.get();
  }

  /**
   * Add a closeable to the list.
   *
   * @param closeable the closeable to add
   * @throws IllegalStateException if the list is already closed
   * @throws NullPointerException if the argument is null
   */
  public void add(Closeable closeable) throws IllegalStateException, NullPointerException {
    if (closed.get()) {
      throw new IllegalStateException("Already closed");
    }
    if (closeable == null) {
      throw new NullPointerException("No null closeable accepted");
    }
    if (!closeables.contains(closeable)) {
      closeables.add(closeable);
    }
  }

  public void close() {
    if (closed.compareAndSet(false, true)) {
      for (Closeable closeable : closeables) {
        log.debug("Closing " + closeable.getClass().getSimpleName());
        Safe.close(closeable);
      }
    }
  }
}
