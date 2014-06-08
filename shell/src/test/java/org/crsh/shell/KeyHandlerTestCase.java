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
package org.crsh.shell;

import test.shell.base.BaseProcessContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Julien Viet
 */
public class KeyHandlerTestCase extends AbstractShellTestCase {

  public void testCommand() throws Exception {
    testKeyHandler("public class foo implements org.crsh.keyboard.KeyHandler {\n" +
        "public void handle(org.crsh.keyboard.KeyType type, int[] sequence) { queue.add(sequence); latch2.countDown(); }\n" +
        "@Command\n" +
        "public void main() {\n" +
        "latch1.countDown();" +
        "latch2.await(10, java.util.concurrent.TimeUnit.SECONDS);" +
        "}\n" +
        "}\n");

  }

  public void testPipeCommand() throws Exception {
    testKeyHandler("public class foo {\n" +
        "  static class PipeImpl extends org.crsh.command.Pipe<Object, String> implements org.crsh.keyboard.KeyHandler {\n" +
        "    java.util.concurrent.CountDownLatch l1\n" +
        "    java.util.concurrent.CountDownLatch l2\n" +
        "    java.util.concurrent.ArrayBlockingQueue q\n" +
        "    PipeImpl(java.util.concurrent.ArrayBlockingQueue queue, java.util.concurrent.CountDownLatch latch1, java.util.concurrent.CountDownLatch latch2) {\n" +
        "      q = queue;\n" +
        "      l1 = latch1;\n" +
        "      l2 = latch2;\n" +
        "    }\n" +
        "    public void handle(org.crsh.keyboard.KeyType type, int[] sequence) { q.add(sequence); l2.countDown(); }\n" +
        "    public void close() {\n" +
        "      l1.countDown();" +
        "      l2.await(10, java.util.concurrent.TimeUnit.SECONDS);" +
        "    }\n" +
        "  }\n" +
        "  @Command\n" +
        "  public org.crsh.command.Pipe<Object, String> main() {\n" +
        "    return new PipeImpl(queue, latch1, latch2);\n" +
        "  }\n" +
        "}\n");
  }

  private void testKeyHandler(String command) throws Exception {
    CountDownLatch latch1 = new CountDownLatch(1);
    ArrayBlockingQueue<int[]> queue = new ArrayBlockingQueue<int[]>(1);
    groovyShell.setVariable("queue", queue);
    groovyShell.setVariable("latch1", latch1);
    groovyShell.setVariable("latch2", new CountDownLatch(1));
    lifeCycle.bindGroovy("foo", command);
    final BaseProcessContext process = create("foo");
    new Thread() {
      @Override
      public void run() {
        process.execute();
      }
    }.start();
    latch1.await(10, TimeUnit.SECONDS);
    process.on(null, new int[]{'a'});
    ShellResponse response = process.getResponse();
    assertInstance(ShellResponse.Ok.class, response);
    int[] event = queue.poll(10, TimeUnit.SECONDS);
    assertEquals(1, event.length);
    assertEquals('a', event[0]);
  }
}
