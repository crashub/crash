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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CloseableList implements Closeable {

  /** . */
  final Logger log = Logger.getLogger(CloseableList.class.getName());

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
        log.log(Level.FINE, "Closing " + closeable.getClass().getSimpleName());
        Utils.close(closeable);
      }
    }
  }
}
