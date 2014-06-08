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
import test.shell.sync.SyncProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Julien Viet
 */
public class ViEofMaybeTestCase extends AbstractConsoleTestCase {

  public void testCtrlD1() throws Exception {
    final ArrayBlockingQueue<String> requests = new ArrayBlockingQueue<String>(1);
    SyncProcess process = new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        context.end(ShellResponse.ok());
      }
    };
    console.init();
    console.toInsert();
    for (int i = 0;i < 4;i++) {
      shell.addProcess(process);
      console.on(KeyStrokes.of("abc"));
      for (int j = 0;j < i;j++) {
        console.on(KeyStrokes.LEFT);
      }
      console.on(Operation.VI_EOF_MAYBE);
      String request = requests.poll(1, TimeUnit.SECONDS);
      assertEquals("abc", request);
      assertEquals(true, console.isRunning());
    }
  }

  public void testCtrlD2() throws Exception {
    console.init();
    console.toInsert();
    console.on(Operation.VI_EOF_MAYBE);
    assertEquals(false, console.isRunning());
  }
}
