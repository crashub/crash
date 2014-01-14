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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.console.ConsoleTerm;
import org.crsh.term.spi.TestTermIO;
import org.crsh.text.CLS;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProcessorTestCase extends TestCase {

  public void testLine() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("abc\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertChars("c").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("abc").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testDel() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("abc");
    controller.connector.appendDel();
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertChars("c").assertFlush();
    controller.connector.assertDel().assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("ab").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testBreak() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("abc");
    controller.connector.appendBreak();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertChars("c").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.connector.append("def\r\n");
    controller.connector.assertChars("d").assertFlush();
    controller.connector.assertChars("e").assertFlush();
    controller.connector.assertChars("f").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("def").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testInsert() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("ab");
    controller.connector.appendMoveLeft();
    controller.connector.append("c\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertMoveLeft().assertFlush();
    controller.connector.assertChars("cb").assertMoveLeft().assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("acb").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testIdempotentMoveRight() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.appendMoveRight();
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testIdempotentMoveLeft() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.appendMoveLeft();
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testMove() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.connector.appendMoveUp();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.connector.appendMoveUp();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.appendMoveDown();
    controller.connector.assertDel().assertFlush();

    //
    controller.assertStop();
  }

  public void testIdempotentMoveUp() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.connector.appendMoveUp();
    controller.connector.appendMoveUp();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testIdempotentMoveDown() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.connector.appendMoveDown();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testCompletion1() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO) {
      @Override
      public CompletionMatch complete(String prefix) {
        return new CompletionMatch(Delimiter.EMPTY, Completion.create(new StringBuilder(prefix).reverse().toString(), false));
      }
    });
    controller.assertStart();

    //
    controller.connector.append("ab");
    controller.connector.appendTab();
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertChars("ba").assertFlush();
  }

  public void testMultiLine() throws Exception {
    final LinkedList<String> requests = new LinkedList<String>();
    final CountDownLatch latch = new CountDownLatch(1);
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO) {
      @Override
      public ShellProcess createProcess(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            requests.add(request);
            latch.countDown();
            return super.execute(request);
          }
        };
      }
    });
    controller.assertStart();

    //
    controller.connector.append("a\\\r\n");
    controller.connector.assertChars("a").assertFlush();
    controller.connector.assertChars("\\").assertFlush();
    controller.connector.assertCRLF().assertFlush().assertChars("> ").assertFlush();
    assertEquals(Collections.<String>emptyList(), requests);
    controller.connector.append("b\r\n");
    controller.connector.assertChars("b").assertFlush();
    controller.connector.assertCRLF().assertFlush();
    latch.await(5, TimeUnit.SECONDS);
    assertEquals(Collections.singletonList("ab"), requests);
  }

  public void testCLS() throws Exception {
    Controller controller = create(new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, ShellProcessContext processContext) throws IOException {
            if ("bye".equals(request)) {
              processContext.end(ShellResponse.close());
            } else {
              processContext.write(CLS.INSTANCE);
              processContext.end(ShellResponse.ok());
            }
          }
        };
      }
    }));

    //
    controller.assertStart();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertCLS().assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  public void testFlush() throws Exception {
    Controller controller = create(new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, ShellProcessContext processContext) throws IOException {
            if ("bye".equals(request)) {
              processContext.end(ShellResponse.close());
            } else {
              processContext.flush();
              processContext.end(ShellResponse.ok());
            }
          }
        };
      }
    }));

    //
    controller.assertStart();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF().assertFlush();
    controller.connector.assertFlush(); // The good one
    controller.connector.assertFlush();
    controller.connector.assertCRLF().assertChars("% ").assertFlush();

    //
    controller.assertStop();
  }

  private Controller create(Shell shell) throws IOException {
    return new Controller(new TestTermIO(), shell);
  }

  private class Controller implements Runnable {

    /** . */
    private volatile boolean running;

    /** . */
    private final CountDownLatch startSync;

    /** . */
    private final CountDownLatch stopSync;

    /** . */
    private final Thread thread;

    /** . */
    private final TestTermIO connector;

    /** . */
    private final Processor processor;

    private Controller(TestTermIO connector, Shell shell) {
      this.running = true;
      this.startSync = new CountDownLatch(1);
      this.stopSync = new CountDownLatch(1);
      this.thread = new Thread(this);
      this.connector = connector;
      this.processor = new Processor(new ConsoleTerm(connector), shell);
    }

    public void assertStart() {
      thread.start();

      //
      try {
        assertTrue(startSync.await(1, TimeUnit.SECONDS));
      }
      catch (InterruptedException e) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(e);
        throw afe;
      }

      //
      assertTrue(running);

      //
      connector.assertCRLF();
      connector.assertChars("% ");
      connector.assertFlush();
    }

    public void run() {
      running = true;
      startSync.countDown();
      try {
        processor.run();
      }
      finally {
        running = false;
        stopSync.countDown();
      }
    }

    public void assertStop() {
      assertTrue(running);

      //
      connector.append("bye\r\n");
      connector.assertChars("b").assertFlush();
      connector.assertChars("y").assertFlush();
      connector.assertChars("e").assertFlush();
      connector.assertCRLF().assertFlush();

      //
      try {
        assertTrue(stopSync.await(4, TimeUnit.SECONDS));
      }
      catch (InterruptedException e) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(e);
        throw afe;
      }

      //
      connector.assertEmpty();

      //
      assertFalse(running);
    }
  }
}
