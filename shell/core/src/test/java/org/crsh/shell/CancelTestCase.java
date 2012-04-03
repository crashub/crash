package org.crsh.shell;

import org.crsh.BaseProcessContext;

import java.util.concurrent.CountDownLatch;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CancelTestCase extends AbstractCommandTestCase {


  public void testInterrupt() {
    final BaseProcessContext ctx = create("sleep 100");
    Thread t = new Thread() {
      @Override
      public void run() {
        ctx.execute();
      }
    };
    t.start();

    //
    while (t.getState() == Thread.State.WAITING) {
      // Waiting
    }

    ctx.cancel();
    ShellResponse resp = ctx.getResponse();
    assertEquals(ShellResponse.Cancelled.class, resp.getClass());
  }

  public void testLoop() throws Exception {
    final BaseProcessContext ctx = create("invoke " + CancelTestCase.class.getName() + " testLoopCallback");
    Thread t = new Thread() {
      @Override
      public void run() {
        ctx.execute();
      }
    };

    //
    latch1 = new CountDownLatch(1);
    latch2 = true;
    interrupted = null;
    t.start();

    //
    latch1.await();
    ctx.cancel();
    latch2 = false;

    //
    ShellResponse resp = ctx.getResponse();
    assertEquals(ShellResponse.Cancelled.class, resp.getClass());
    assertEquals(Boolean.TRUE, interrupted);
  }

  /** . */
  private static CountDownLatch latch1;

  /** . */
  private static volatile boolean latch2;

  /** . */
  private static volatile Boolean interrupted;

  public static void testLoopCallback() {
    latch1.countDown();
    while (latch2) {
      //
    }
    interrupted = Thread.currentThread().isInterrupted();
  }
}
