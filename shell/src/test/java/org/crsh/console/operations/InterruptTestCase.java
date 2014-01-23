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
package org.crsh.console.operations;

import jline.console.Operation;
import org.crsh.console.AbstractConsoleTestCase;
import org.crsh.console.KeyEvent;
import org.crsh.console.KeyEvents;
import org.crsh.console.Status;
import org.crsh.processor.term.SyncProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Julien Viet
 */
public class InterruptTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    doTest(Status.Emacs.class, Status.Emacs.class);
  }

  public void testInsert() {
    console.init();
    console.toInsert();
    doTest(Status.Insert.class, Status.Insert.class);
  }

  public void testMove() {
    console.init();
    console.toMove();
    doTest(Status.Move.class, Status.Move.class);
  }

  public void testDigit() {
    console.init();
    console.toMove();
    console.on(Operation.VI_ARG_DIGIT, '3');
    doTest(Status.Digit.class, Status.Move.class);
  }

  public void testDeleteTo() {
    console.init();
    console.toMove();
    console.on(Operation.VI_DELETE_TO, 'd');
    doTest(Status.DeleteTo.class, Status.Move.class);
  }

  public void testChangeTo() {
    console.init();
    console.toMove();
    console.on(Operation.VI_CHANGE_TO, 'c');
    doTest(Status.ChangeTo.class, Status.Move.class);
  }

  public void testYankToChar() {
    console.init();
    console.toMove();
    console.on(Operation.VI_YANK_TO);
    doTest(Status.YankTo.class, Status.Move.class);
  }

  public void testChangeChar() {
    console.init();
    console.toMove();
    console.on(Operation.VI_CHANGE_CHAR);
    doTest(Status.ChangeChar.class, Status.Move.class);
  }

  private void doTest(Class<? extends Status> prevStatus ,Class<? extends Status> nextStatus) {
    assertInstance(prevStatus, console.getMode());
    console.on(KeyEvent.of("foo"));
    console.on(Operation.INTERRUPT);
    assertInstance(nextStatus, console.getMode());
    assertEquals("", getCurrentLine());
    assertEquals(0, getCurrentCursor());
  }

  public void testProcess() throws Exception {
    final ArrayBlockingQueue<ShellProcessContext> contexts = new ArrayBlockingQueue<ShellProcessContext>(1);
    final CountDownLatch latch = new CountDownLatch(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        contexts.add(context);
      }
      @Override
      public void cancel() {
        latch.countDown();
      }
    });
    console.init();
    console.on(KeyEvent.of("foo"));
    console.on(KeyEvents.ENTER);
    console.on(KeyEvent.of("a"));
    console.on(KeyEvents.INTERRUPT);
    latch.await();
    ShellProcessContext context = contexts.poll(1, TimeUnit.SECONDS);
    context.end(ShellResponse.ok());
    assertEquals("a", getCurrentLine());
    assertEquals(1, getCurrentCursor());
  }
}
