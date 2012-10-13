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

package org.crsh.shell;

import org.crsh.AbstractTestCase;
import org.crsh.BaseProcessContext;

import java.util.concurrent.CountDownLatch;

public class CancellationTestCase extends AbstractCommandTestCase {

  /** . */
  private static final Object interrupLock = new Object();

  /** . */
  private static boolean interruptDoCancel = false;

  /** . */
  private static boolean interruptInterrupted = false;

  public static void interruptCallback() {
    synchronized (interrupLock) {
      interruptDoCancel = true;
      interrupLock.notifyAll();
      try {
        interrupLock.wait(10 * 1000);
      }
      catch (InterruptedException e) {
        interruptInterrupted = true;
      }
    }
  }

  public void testInterrupt() {
    final BaseProcessContext ctx = create("invoke " + CancellationTestCase.class.getName() + " interruptCallback");
    Thread t = new Thread() {
      @Override
      public void run() {
        ctx.execute();
      }
    };
    t.start();

    //
    synchronized (interrupLock) {
      if (!interruptDoCancel) {
        try {
          interrupLock.wait(10 * 1000);
        }
        catch (InterruptedException e) {
          throw AbstractTestCase.failure(e);
        }
      }
    }

    // We should have been interrupted
    assertTrue(interruptDoCancel);

    //
    ctx.cancel();
    ShellResponse resp = ctx.getResponse();
    assertEquals(ShellResponse.Cancelled.class, resp.getClass());
    assertTrue(interruptInterrupted);
  }

  public void testLoop() throws Exception {
    final BaseProcessContext ctx = create("invoke " + CancellationTestCase.class.getName() + " loopCallback");
    Thread t = new Thread() {
      @Override
      public void run() {
        ctx.execute();
      }
    };

    //
    loopLatch1 = new CountDownLatch(1);
    loopLatch2 = true;
    loopInterrupted = null;
    t.start();

    //
    loopLatch1.await();
    ctx.cancel();
    loopLatch2 = false;

    //
    ShellResponse resp = ctx.getResponse();
    assertEquals(ShellResponse.Cancelled.class, resp.getClass());
    assertEquals(Boolean.TRUE, loopInterrupted);
  }

  /** . */
  private static CountDownLatch loopLatch1;

  /** . */
  private static volatile boolean loopLatch2;

  /** . */
  private static volatile Boolean loopInterrupted;

  public static void loopCallback() {
    loopLatch1.countDown();
    while (loopLatch2) {
      //
    }
    loopInterrupted = Thread.currentThread().isInterrupted();
  }
}
