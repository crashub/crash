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

import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SyncShellResponseContext implements ShellResponseContext {

  /** . */
  private final ShellResponseContext wrapped;

  /** . */
  private ShellResponse response;

  /** . */
  private final CountDownLatch latch;

  public SyncShellResponseContext() {
    this(null);
  }

  public SyncShellResponseContext(ShellResponseContext wrapped) {
    this.wrapped = wrapped;
    this.latch = new CountDownLatch(1);
    this.response = null;
  }

  public String readLine(String msg, boolean echo) {
    if (wrapped != null) {
      return wrapped.readLine(msg, echo);
    } else {
      return null;
    }
  }

  public void done(ShellResponse response) {
    this.response = response;
    this.latch.countDown();

    //
    if (wrapped != null) {
      wrapped.done(response);
    }
  }

  public ShellResponse getResponse() throws InterruptedException {
    latch.await();
    return response;
  }
}
