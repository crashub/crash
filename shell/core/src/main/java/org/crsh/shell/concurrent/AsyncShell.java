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
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellProcessContext;

import java.util.Map;
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
  private Process current;

  /** . */
  private final ExecutorService executor;

  public AsyncShell(ExecutorService executor, Shell shell) {
    this.lock = new Object();
    this.status = Status.AVAILABLE;
    this.shell = shell;
    this.current = null;
    this.executor = executor;
  }

  public Status getStatus() {
    synchronized (lock) {
      return status;
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

  private class Process implements ShellProcessContext, Runnable, ShellProcess {


    /** . */
    private final String request;

    /** . */
    private ShellProcessContext caller;

    /** . */
    private ShellProcess callee;

    private Process(String request) {
      this.request = request;
      this.callee = null;
    }

    // ShellProcessContext implementation ******************************************************************************

    public int getWidth() {
      return caller.getWidth();
    }

    public Object getProperty(String name) {
      return caller.getProperty(name);
    }

    public String readLine(String msg, boolean echo) {
      return caller.readLine(msg, echo);
    }

    public void end(ShellResponse response) {

      synchronized (lock) {

        // Signal response
        if (status == Status.CANCELED) {
          caller.end(new ShellResponse.Cancelled());
        } else {
          caller.end(response);
        }

        // Update state
        current = null;
        status = Status.AVAILABLE;
      }
    }

    // ShellProcess implementation *************************************************************************************

    public void execute(ShellProcessContext processContext) {
      synchronized (lock) {

        if (status != Status.AVAILABLE) {
          throw new IllegalStateException("State was " + status);
        }

        //
        // Update state
        status = Status.EVALUATING;
        current = this;
        callee = shell.createProcess(request);
        caller = processContext;
      }

      //
      executor.submit(current);
    }

    public void cancel() {
      synchronized (lock) {
        if (status == Status.EVALUATING) {
          status = Status.CANCELED;
          callee.cancel();
        }
      }
    }

    // Runnable implementation *****************************************************************************************

    public void run() {
      callee.execute(this);
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return shell.getWelcome();
  }

  public String getPrompt() {
    return shell.getPrompt();
  }

  public Map<String, String> complete(String prefix) {
    return shell.complete(prefix);
  }

  public ShellProcess createProcess(String request) {
    return new Process(request);
  }
}
