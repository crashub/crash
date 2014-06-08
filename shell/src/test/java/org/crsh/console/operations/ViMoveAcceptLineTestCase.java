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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Julien Viet
 */
public class ViMoveAcceptLineTestCase extends AbstractConsoleTestCase {

  public void testEnter() {
    console.init();
    final AtomicReference<String> calls = new AtomicReference<String>();
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        calls.set(request);
        context.end(ShellResponse.ok());
      }
    });
    console.toInsert();
    console.on(KeyStrokes.of("abc def"));
    console.toMove();
    console.on(Operation.VI_MOVE_ACCEPT_LINE);
    assertEquals("abc def", calls.get());
    assertEquals(Mode.VI_INSERT, console.getMode());
  }
}
