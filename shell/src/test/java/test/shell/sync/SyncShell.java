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

package test.shell.sync;

import org.crsh.AbstractTestCase;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class SyncShell implements Shell {

  /** . */
  private final LinkedList<Throwable> failures;

  /** . */
  private final Object lock;

  /** . */
  private final LinkedList<SyncProcess> queue;

  /** . */
  private AtomicReference<SyncCompleter> completer;

  public SyncShell() {
    this.lock = new Object();
    this.queue = new LinkedList<SyncProcess>();
    this.failures = new LinkedList<Throwable>();
    this.completer = new AtomicReference<SyncCompleter>();
  }

  public void setCompleter(SyncCompleter completer) {
    this.completer.set(completer);
  }

  public void addProcess(SyncProcess process) {
    synchronized (lock) {
      queue.add(process);
      lock.notifyAll();
    }
  }

  public String getWelcome() {
    return "";
  }

  public String getPrompt() {
    return "";
  }

  public ShellProcess createProcess(final String request) throws IllegalStateException {
    synchronized (lock) {
      while (true) {
        if (queue.size() > 0) {
          final SyncProcess runnable = queue.removeFirst();
          return new ShellProcess() {
            @Override
            public void execute(ShellProcessContext processContext) throws IllegalStateException {
              try {
                runnable.run(request, processContext);
              }
              catch (Exception e) {
                throw AbstractTestCase.failure(e);
              }
            }
            @Override
            public KeyHandler getKeyHandler() throws IllegalStateException {
              return runnable.keyHandler();
            }
            @Override
            public void cancel() throws IllegalStateException {
              runnable.cancel();
            }
          };
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
    SyncCompleter completer = this.completer.get();
    return completer != null ? completer.complete(prefix) : null;
  }

  public void close() throws IOException {
  }
}
