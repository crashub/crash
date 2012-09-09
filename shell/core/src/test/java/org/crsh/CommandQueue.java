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

package org.crsh;

import junit.framework.AssertionFailedError;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class CommandQueue implements Executor {

  /** . */
  private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

  public synchronized void execute(Runnable command) {
    queue.addLast(command);
  }

  public synchronized int getSize() {
    return queue.size();
  }

  public synchronized Future<Runnable> executeAsync() {
    if (queue.size() == 0) {
      throw new AssertionFailedError();
    }
    Runnable runnable = queue.removeFirst();
    FutureTask<Runnable> future = new FutureTask<Runnable>(runnable, runnable);
    new Thread(future).start();
    return future;
  }
}
