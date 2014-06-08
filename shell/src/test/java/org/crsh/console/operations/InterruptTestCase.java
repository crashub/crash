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
import org.crsh.console.KeyStrokes;
import org.crsh.console.Mode;
import test.shell.sync.SyncProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Julien Viet
 */
public class InterruptTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    doTest(Mode.EMACS, Mode.EMACS);
  }

  public void testInsert() {
    console.init();
    doTest(Mode.VI_INSERT, Mode.VI_INSERT);
  }

  public void testMove() {
    console.init();
    doTest(Mode.VI_MOVE, Mode.VI_MOVE);
  }

  public void testDigit() {
    console.init();
    doTest(new Mode.Digit(3), Mode.VI_MOVE);
  }

  public void testDeleteTo() {
    console.init();
    doTest(Mode.DELETE_TO, Mode.VI_MOVE);
  }

  public void testChangeTo() {
    console.init();
    doTest(Mode.CHANGE_TO, Mode.VI_MOVE);
  }

  public void testYankToChar() {
    console.init();
    doTest(Mode.YANK_TO, Mode.VI_MOVE);
  }

  public void testChangeChar() {
    console.init();
    doTest(new Mode.ChangeChar(1), Mode.VI_MOVE);
  }

  private void doTest(Mode status, Mode expectedStatus) {
    console.on(KeyStrokes.of("foo"));
    console.setMode(status);
    assertEquals("foo", getCurrentLine());
    assertEquals(3, getCurrentCursor());
    console.on(Operation.INTERRUPT);
    assertEquals(expectedStatus, console.getMode());
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
    console.on(KeyStrokes.of("foo"));
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.of("a"));
    console.on(KeyStrokes.INTERRUPT);
    latch.await();
    ShellProcessContext context = contexts.poll(1, TimeUnit.SECONDS);
    context.end(ShellResponse.ok());
    assertEquals("a", getCurrentLine());
    assertEquals(1, getCurrentCursor());
  }
}
