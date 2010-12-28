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
  private Foo current;

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

  private class Foo implements ShellProcessContext, Runnable, ShellProcess {


    /** . */
    private final String request;

    /** . */
    private final ShellProcessContext caller;

    /** . */
    private ShellProcess callee;

    private Foo(String request, ShellProcessContext caller) {
      this.request = request;
      this.caller = caller;
      this.callee = null;
    }

    // ShellProcessContext implementation ******************************************************************************

    public String readLine(String msg, boolean echo) {
      return caller.readLine(msg, echo);
    }

    public void begin(ShellProcess process) {
      caller.begin(this);
      callee = process;
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
      shell.process(request, current);
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

  public void process(String request, ShellProcessContext processContext) {

    synchronized (lock) {

      if (status != Status.AVAILABLE) {
        throw new IllegalStateException("State was " + status);
      }

      //
      // Update state
      status = Status.EVALUATING;
      current = new Foo(request, processContext);
    }

    //
    executor.submit(current);
  }
}
