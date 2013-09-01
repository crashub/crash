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

package org.crsh.shell.impl.async;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.Shell;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class AsyncShell implements Shell, Closeable {

  /** . */
  final Shell shell;

  /** . */
  private AsyncProcess current;

  /** . */
  final ExecutorService executor;

  /** . */
  boolean closed;

  /** . */
  final Object lock = new Object();

  /** . */
  final Set<AsyncProcess> processes;

  public AsyncShell(ExecutorService executor, Shell shell) {
    this.shell = shell;
    this.current = null;
    this.executor = executor;
    this.closed = false;
    this.processes = Collections.synchronizedSet(new HashSet<AsyncProcess>());
  }

  public void close() {

    AsyncProcess[] toCancel = null;
    synchronized (lock) {
      if (closed) {
        toCancel = null;
      } else {
        closed = true;
        toCancel = processes.toArray(new AsyncProcess[processes.size()]);
      }
    }

    // Cancel all process
    if (toCancel != null) {
      for (AsyncProcess process : toCancel) {
        process.cancel();
      }
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return shell.getWelcome();
  }

  public String getPrompt() {
    return shell.getPrompt();
  }

  public CompletionMatch complete(String prefix) {
    return shell.complete(prefix);
  }

  public AsyncProcess createProcess(String request) {
    synchronized (lock) {
      if (closed) {
        throw new IllegalStateException();
      }
    }
    return new AsyncProcess(this, request);
  }
}
