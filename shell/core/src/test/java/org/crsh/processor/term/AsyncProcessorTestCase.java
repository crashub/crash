package org.crsh.processor.term;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.term.TermEvent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AsyncProcessorTestCase extends AbstractProcessorTestCase {

  @Override
  protected SyncShell createShell() {
    return new SyncShell();
  }

  @Override
  protected SyncTerm createTerm() {
    return new SyncTerm();
  }

  @Override
  protected Processor createProcessor(SyncTerm term, SyncShell shell) {
    AsyncShell async = new AsyncShell(Executors.newSingleThreadExecutor(), shell);
    return new Processor(term, async);
  }

  @Override
  protected int getBarrierSize() {
    return 2;
  }

  public void testCloseHangingProcess() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(2);
    final CyclicBarrier syncC = new CyclicBarrier(2);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellProcess() {
      public void execute(ShellProcessContext processContext) {
        try {
          syncA.await();
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      public void cancel() {
        try {
          syncC.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    syncA.await();
    term.publish(TermEvent.brk());
    syncC.await();
    term.publish(TermEvent.close());
    assertJoin(thread);
    syncB.await();
  }

  public void testBreak() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(3);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellProcess() {
      public void execute(ShellProcessContext processContext) {
        try {
          syncA.await();
          syncB.await();
        }
        catch (Exception e) {
          throw failure(e);
        }
      }

      public void cancel() {
        try {
          syncB.await();
        }
        catch (Exception e) {
          throw failure(e);
        }
      }
    });
    syncA.await();
    term.publish(TermEvent.brk());
    syncB.await();
    term.publish(TermEvent.close());
    assertJoin(thread);
  }
}
