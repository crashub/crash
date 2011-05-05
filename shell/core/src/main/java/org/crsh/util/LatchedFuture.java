/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LatchedFuture<V> implements Future<V> {

  /** . */
  private V value = null;

  /** . */
  private final CountDownLatch latch = new CountDownLatch(1);

  /** . */
  private final Lock lock = new ReentrantLock();

  /** The optional listener. */
  private FutureListener<V> listener;

  public LatchedFuture() {
  }

  public LatchedFuture(V value) {
    set(value);
  }

  public boolean cancel(boolean b) {
    return false;
  }

  public boolean isCancelled() {
    return false; 
  }

  public boolean isDone() {
    return latch.getCount() == 0;
  }

  public V get() throws InterruptedException, ExecutionException {
    latch.await();
    return value;
  }

  public V get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
    latch.await(l, timeUnit);
    return value;
  }

  public void addListener(FutureListener<V> listener) {
    if (listener == null) {
      throw new NullPointerException();
    }
    lock.lock();
    try {

      // Avoid double init
      if (this.listener != null) {
        throw new IllegalStateException();
      }

      // Set state
      this.listener = listener;

      //
      if (latch.getCount() == 0) {
        listener.completed(value);
      }

      //
    } finally {
      lock.unlock();
    }
  }

  public void set(V value) {
    lock.lock();
    try {
      if (latch.getCount() > 0) {
        this.value = value;
        latch.countDown();
        if (listener != null) {
          listener.completed(value);
        }
      } else {
        throw new IllegalStateException();
      }
    } finally {
      lock.unlock();
    }
  }
}
