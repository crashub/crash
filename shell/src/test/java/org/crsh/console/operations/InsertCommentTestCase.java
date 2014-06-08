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
public class InsertCommentTestCase extends AbstractConsoleTestCase {

  public void testEmacs() throws Exception {
    final ArrayBlockingQueue<String> requests = new ArrayBlockingQueue<String>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        context.end(ShellResponse.ok());
      }
    });
    console.init();
    console.on(KeyStrokes.of("putrified whales"));
    console.on(Operation.INSERT_COMMENT);
    String request = requests.poll(5, TimeUnit.SECONDS);
    assertEquals("#putrified whales", request);
  }

  // Vi move

  // The # key causes a comment to get inserted.
  public void testInsertComment1() throws Exception {
    final ArrayBlockingQueue<String> requests = new ArrayBlockingQueue<String>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        context.end(ShellResponse.ok());
      }
    });
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("putrified whales"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_INSERT_COMMENT);
    String request = requests.poll(5, TimeUnit.SECONDS);
    assertEquals("#putrified whales", request);
  }

  public void testInsertComment2() throws Exception {
    final ArrayBlockingQueue<String> requests = new ArrayBlockingQueue<String>(1);
    shell.addProcess(new SyncProcess() {
      @Override
      public void run(String request, ShellProcessContext context) throws Exception {
        requests.add(request);
        context.end(ShellResponse.ok());
      }
    });
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("echo \"abc"));
    console.on(KeyStrokes.ENTER);
    console.on(KeyStrokes.of("def"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_INSERT_COMMENT);
    console.on(KeyStrokes.QUOTE);
    console.on(KeyStrokes.ENTER);
    String request = requests.poll(5, TimeUnit.SECONDS);
    assertEquals("echo \"abc\n#def\n\"", request);
  }
}
