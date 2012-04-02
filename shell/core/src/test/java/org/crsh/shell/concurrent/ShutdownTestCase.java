package org.crsh.shell.concurrent;

import org.crsh.AbstractTestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.CommandQueue;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ShutdownTestCase extends AbstractTestCase {

  public void testCreate() throws Exception {
    Shell shell = new BaseShell();
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);
    asyncShell.close();
    try {
      asyncShell.createProcess("foo");
    }
    catch (IllegalStateException e) {
    }
  }

  public void testExecute() throws Exception {
    Shell shell = new BaseShell();
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);
    AsyncProcess process = asyncShell.createProcess("foo");
    asyncShell.close();
    SyncShellResponseContext ctx = new SyncShellResponseContext();
    process.execute(ctx);
    assertEquals(Status.TERMINATED, process.getStatus());
    assertEquals(ShellResponse.Cancelled.class, ctx.getResponse().getClass());
    assertEquals(0, commands.getSize());
  }

  public void testCancel() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    final CountDownLatch latch1 = new CountDownLatch(1);
    final CountDownLatch latch2 = new CountDownLatch(1);

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            latch1.countDown();
            try {
              latch2.await();
            } catch (InterruptedException e) {
              failure.set(e);
            }
            return new ShellResponse.Ok();
          }
          @Override
          public void cancel() {
            latch2.countDown();
          }
        };
      }
    };

    //
    Shell shell = new BaseShell(factory);
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);

    //
    SyncShellResponseContext respCtx = new SyncShellResponseContext();
    AsyncProcess process = asyncShell.createProcess("foo");
    process.execute(respCtx);
    Future<?> future = commands.executeAsync();
    latch1.await();
    assertEquals(Status.EVALUATING, process.getStatus());

    //
    asyncShell.close();
    assertEquals(Status.CANCELED, process.getStatus());
    assertEquals(ShellResponse.Cancelled.class, respCtx.getResponse().getClass());
  }
}
