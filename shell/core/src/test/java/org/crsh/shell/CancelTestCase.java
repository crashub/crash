package org.crsh.shell;

import org.crsh.AbstractTestCase;
import org.crsh.BaseProcessContext;

import java.util.concurrent.CountDownLatch;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CancelTestCase extends AbstractCommandTestCase {

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
    final BaseProcessContext ctx = create("invoke " + CancelTestCase.class.getName() + " interruptCallback");
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
    final BaseProcessContext ctx = create("invoke " + CancelTestCase.class.getName() + " loopCallback");
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
