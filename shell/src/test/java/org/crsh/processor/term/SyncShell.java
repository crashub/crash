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
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;

import java.io.IOException;
import java.util.LinkedList;

public class SyncShell implements Shell {

  /** . */
  private final LinkedList<Throwable> failures;

  /** . */
  private final Object lock;

  /** . */
  private final LinkedList<ShellProcess> queue;

  public SyncShell() {
    this.lock = new Object();
    this.queue = new LinkedList<ShellProcess>();
    this.failures = new LinkedList<Throwable>();
  }

  public void publish(final ShellRunnable callable) {
    publish(new ShellProcess() {
      public void execute(ShellProcessContext processContext) {
        try {
          callable.run(processContext);
        }
        catch (Exception e) {
          throw AbstractTestCase.failure(e);
        }
      }
      public void cancel() {
      }
    });
  }

  public void publish(ShellProcess process) {
    synchronized (lock) {
      queue.add(process);
      lock.notifyAll();
    }
  }

  public String getWelcome() {
    return "welcome";
  }

  public String getPrompt() {
    return "%";
  }

  public ShellProcess createProcess(String request) throws IllegalStateException {
    synchronized (lock) {
      while (true) {
        if (queue.size() > 0) {
          return queue.removeFirst();
        } else {
          try {
            lock.wait();
          }
          catch (InterruptedException e) {
            throw AbstractTestCase.failure(e);
          }
        }
      }
    }
  }

  public CompletionMatch complete(String prefix) {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
  }
}
