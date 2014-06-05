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

import org.crsh.keyboard.KeyHandler;
import org.crsh.text.Screenable;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.concurrent.Callable;

public class AsyncProcess implements ShellProcess {


  /** . */
  private final String request;

  /** . */
  private ShellProcessContext caller;

  /** . */
  private ShellProcess callee;

  /** . */
  private AsyncShell shell;

  /** . */
  private Status status;

  /** . */
  private final Object lock;

  AsyncProcess(AsyncShell shell, String request) {
    this.shell = shell;
    this.request = request;
    this.callee = null;
    this.status = Status.CONSTRUCTED;
    this.lock = new Object();
  }

  public Status getStatus() {
    return status;
  }

  /** . */
  private final ShellProcessContext context = new ShellProcessContext() {
    public int getWidth() {
      return caller.getWidth();
    }

    public int getHeight() {
      return caller.getHeight();
    }

    public String getProperty(String name) {
      return caller.getProperty(name);
    }

    public boolean takeAlternateBuffer() throws IOException {
      return caller.takeAlternateBuffer();
    }

    public boolean releaseAlternateBuffer() throws IOException {
      return caller.releaseAlternateBuffer();
    }

    public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
      return caller.readLine(msg, echo);
    }

    public Screenable append(CharSequence s) throws IOException {
      caller.append(s);
      return this;
    }

    @Override
    public Screenable append(char c) throws IOException {
      caller.append(c);
      return this;
    }

    @Override
    public Screenable append(CharSequence csq, int start, int end) throws IOException {
      caller.append(csq, start, end);
      return this;
    }

    public Screenable append(Style style) throws IOException {
      caller.append(style);
      return this;
    }

    public Screenable cls() throws IOException {
      caller.cls();
      return this;
    }

    public void flush() throws IOException {
      caller.flush();
    }

    public void end(ShellResponse response) {
      // Always leave the status in terminated status if the method succeeds
      // Cancelled -> Terminated
      // Evaluating -> Terminated
      // Terminated -> Terminated
      synchronized (lock) {
        switch (status) {
          case CONSTRUCTED:
          case QUEUED:
            throw new AssertionError("Should not happen");
          case CANCELED:
            // We substitute the response
            response = ShellResponse.cancelled();
            status = Status.TERMINATED;
            break;
          case EVALUATING:
            status = Status.TERMINATED;
            break;
          case TERMINATED:
            throw new IllegalStateException("Cannot end a process already terminated");
        }
      }

      //
      caller.end(response);
    }
  };

  @Override
  public KeyHandler getKeyHandler() {
    synchronized (lock) {
      if (status != Status.EVALUATING) {
        throw new IllegalStateException();
      }
    }
    // Should it be synchronous or not ????
    // no clue for now :-)
    return callee.getKeyHandler();
  }

  public void execute(ShellProcessContext processContext) {

    // Constructed -> Queued
    synchronized (lock) {
      if (status != Status.CONSTRUCTED) {
        throw new IllegalStateException("State was " + status);
      }

      // Update state
      status = Status.QUEUED;
      callee = shell.shell.createProcess(request);
      caller = processContext;
    }

    // Create the task
    Callable<AsyncProcess> task = new Callable<AsyncProcess>() {
      public AsyncProcess call() throws Exception {
        try {
          // Cancelled -> Cancelled
          // Queued -> Evaluating
          ShellResponse response;
          synchronized (lock) {
            switch (status) {
              case CANCELED:
                // Do nothing it was canceled in the mean time
                response = ShellResponse.cancelled();
                break;
              case QUEUED:
                // Ok we are going to run it
                status = Status.EVALUATING;
                response = null;
                break;
              default:
                // Just in case but this can only be called by the queue
                throw new AssertionError();
            }
          }

          // Execute the process if we are in evalating state
          if (response == null) {
            // Here the status could already be in status cancelled
            // it is a race condition, execution still happens
            // but the callback of the callee to the end method will make the process
            // terminate and use a cancel response
            try {
              callee.execute(context);
              response = ShellResponse.ok();
            }
            catch (Throwable t) {
              response = ShellResponse.internalError("Unexpected throwable when executing process", t);
            }
          }

          // Make the callback
          // Calling terminated twice will have no effect
          try {
            context.end(response);
          }
          catch (Throwable t) {
            // Log it
          }

          // We return this but we don't really care for now
          return AsyncProcess.this;
        }
        finally {
          synchronized (shell.lock) {
            shell.processes.remove(AsyncProcess.this);
          }
        }
      }
    };

    //
    synchronized (shell.lock) {
      if (!shell.closed) {
        shell.executor.submit(task);
        shell.processes.add(this);
      } else {
        boolean invokeEnd;
        synchronized (lock) {
          invokeEnd = status != Status.TERMINATED;
          status = Status.TERMINATED;
        }
        if (invokeEnd) {
          caller.end(ShellResponse.cancelled());
        }
      }
    }
  }

  public void cancel() {
    // Construcuted -> ISE
    // Evaluating -> Canceled
    // Queued -> Canceled
    // Cancelled -> Cancelled
    // Terminated -> Terminated
    boolean cancel;
    synchronized (lock) {
      switch (status) {
        case CONSTRUCTED:
          throw new IllegalStateException("Cannot call cancel on process that was not scheduled for execution yet");
        case QUEUED:
          status = Status.CANCELED;
          cancel = false;
          break;
        case EVALUATING:
          status = Status.CANCELED;
          cancel = true;
          break;
        default:
          cancel = false;
          break;
      }
    }

    //
    if (cancel) {
      callee.cancel();
    }
  }
}
