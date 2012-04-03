package org.crsh.shell.concurrent;

import org.crsh.AbstractTestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessContext;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.CommandQueue;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class FailureTestCase extends AbstractTestCase {

  public void testEvaluating() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    final AtomicInteger cancelCount = new AtomicInteger(0);

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            throw new RuntimeException();
          }
          @Override
          public void cancel() {
            failure.set(failure("Was expecting no cancel callback"));
          }
        };
      }
    };

    //
    Shell shell = new BaseShell(factory);
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);

    //
    BaseProcessContext ctx = BaseProcessContext.create(asyncShell, "foo").execute();
    assertEquals(Status.QUEUED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(0, cancelCount.get());
    assertEquals(1, commands.getSize());

    // Execute the command
    // And wait until the other thread is waiting
    Future<?> future = commands.executeAsync();
    future.get();

    //
    assertEquals(ShellResponse.Error.class, ctx.getResponse().getClass());
  }
}
