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

package org.crsh.processor.term;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.impl.async.AsyncShell;
import org.crsh.term.TermEvent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

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
