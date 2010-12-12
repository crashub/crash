/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.shell.concurrent;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AsyncShell implements Shell {

  /** . */
  private final Object lock;

  /** . */
  private Status status;

  /** . */
  private Shell shell;

  /** . */
  private Foo current;

  /** . */
  private final ExecutorService executor;

  public AsyncShell(ExecutorService executor, Shell shell) {
    this.lock = new Object();
    this.status = Status.INITIAL;
    this.shell = shell;
    this.current = null;
    this.executor = executor;
  }

  public Status getStatus() {
    synchronized (lock) {
      return status;
    }
  }

  public String open() {
    synchronized (lock) {
      if (status != Status.INITIAL) {
        throw new IllegalStateException("Cannot open shell in state " + status);
      }
      String ret = shell.getWelcome();
      status = Status.AVAILABLE;
      return ret;
    }
  }

  public boolean cancel() {
    synchronized (lock) {
      boolean cancelled;
      if (status == Status.EVALUATING) {
        status = Status.CANCELED;
        cancelled = true;
      } else {
        cancelled = false;
      }
      return cancelled;
    }
  }

  public void close() {
    synchronized (lock) {
      switch (status) {
        case INITIAL:
        case AVAILABLE:
          break;
        case CANCELED:
        case EVALUATING:
          throw new UnsupportedOperationException("todo :-) " + status);
        case CLOSED:
          break;
      }
      status = Status.CLOSED;
    }
  }

  private class Foo implements ShellResponseContext, Runnable {


    /** . */
    private final String request;

    /** . */
    private final ShellResponseContext caller;

    private Foo(String request, ShellResponseContext caller) {
      this.request = request;
      this.caller = caller;
    }

    public String readLine(String msg, boolean echo) {
      return caller.readLine(msg, echo);
    }
    public void done(ShellResponse response) {

      synchronized (lock) {

        // Signal response
        if (status == Status.CANCELED) {
          caller.done(new ShellResponse.Cancelled());
        } else {
          caller.done(response);
        }

        // Update state
        Status nextStatus = response instanceof ShellResponse.Close ? Status.CLOSED : Status.AVAILABLE;
        current = null;
        status = nextStatus;
      }
    }

    public void run() {
      shell.evaluate(request, current);
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return shell.getWelcome();
  }

  public String getPrompt() {
    return shell.getPrompt();
  }

  public List<String> complete(String prefix) {
    return shell.complete(prefix);
  }

  public void evaluate(String request, ShellResponseContext responseContext) {

    synchronized (lock) {

      if (status != Status.AVAILABLE) {
        throw new IllegalStateException("State was " + status);
      }

      //
      // Update state
      status = Status.EVALUATING;
      current = new Foo(request, responseContext);
    }

    //
    executor.submit(current);
  }
}
