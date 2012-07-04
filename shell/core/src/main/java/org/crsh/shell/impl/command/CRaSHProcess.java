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
package org.crsh.shell.impl.command;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class CRaSHProcess implements ShellProcess {

  /** . */
  protected final CRaSHSession crash;

  /** . */
  protected final String request;

  /** . */
  private volatile Thread thread;

  /** . */
  private volatile boolean cancelled;

  protected CRaSHProcess(CRaSHSession crash, String request) {
    this.crash = crash;
    this.request = request;
  }

  public void execute(ShellProcessContext processContext) {
    ClassLoader previous = crash.setCRaSHLoader();
    try {
      ShellResponse resp;
      thread = Thread.currentThread();

      //
      if (crash.user != null) {
        String userName = crash.user != null ? crash.user.getName() : "unauthenticated";
        CRaSHSession.accessLog.debug("User " + userName + " executes " + request);
      }

      //
      try {
        try {
          resp = doInvoke(processContext);
          if (Thread.interrupted() || cancelled) {
            throw new InterruptedException("like a goto");
          }
        }
        catch (InterruptedException e) {
          resp = ShellResponse.cancelled();
        }
      } catch (Throwable t) {
        resp = ShellResponse.internalError("Unexpected error when executing process", t);
      } finally {
        thread = null;
      }

      //
      processContext.end(resp);

      //
      if (resp instanceof ShellResponse.Error) {
        ShellResponse.Error error = (ShellResponse.Error)resp;
        Throwable t = error.getThrowable();
        if (t != null) {
          CRaSHSession.log.error("Error while evaluating request '" + request + "' " + error.getData(), t);
        } else {
          CRaSHSession.log.error("Error while evaluating request '" + request + "' " + error.getData());
        }
      }
    }
    finally {
      crash.setPreviousLoader(previous);
    }
  }

  abstract ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException;

  public void cancel() {
    ClassLoader previous = crash.setCRaSHLoader();
    try {
      Thread t = thread;
      if (t != null) {
        t.interrupt();
      }
      cancelled = true;
    }
    finally {
      crash.setPreviousLoader(previous);
    }
  }
}
