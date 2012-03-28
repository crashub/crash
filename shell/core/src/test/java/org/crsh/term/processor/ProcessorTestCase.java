/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.term.processor;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.BaseTerm;
import org.crsh.term.spi.TestTermIO;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ProcessorTestCase extends TestCase {

  public void testLine() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("abc\r\n");
    controller.connector.assertChars("abc");
    controller.connector.assertCRLF();
    controller.connector.assertChars("abc");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

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
    controller.connector.assertChars("abc");
    controller.connector.assertDel();
    controller.connector.assertCRLF();
    controller.connector.assertChars("ab");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.assertStop();
  }

  public void testBreak() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("abc");
    controller.connector.appendBreak();
    controller.connector.assertChars("abc");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.connector.append("def\r\n");
    controller.connector.assertChars("def");
    controller.connector.assertCRLF();
    controller.connector.assertChars("def");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

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
    controller.connector.assertChars("ab");
    controller.connector.assertMoveLeft();
    controller.connector.assertChars("cb");
    controller.connector.assertMoveLeft();
    controller.connector.assertCRLF();
    controller.connector.assertChars("acb");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

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
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

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
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.assertStop();
  }

  public void testMoveUp() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.connector.appendMoveUp();
    controller.connector.assertChars("a");
    controller.connector.append("\r\n");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.assertStop();
  }

  public void testIdempotentMoveUp() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.connector.appendMoveUp();
    controller.connector.appendMoveUp();
    controller.connector.assertChars("a");
    controller.connector.append("\r\n");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.assertStop();
  }

  public void testIdempotentMoveDown() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
    controller.connector.append("a");
    controller.connector.append("\r\n");
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("a");
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.connector.appendMoveDown();
    controller.connector.append("\r\n");
    controller.connector.assertCRLF();
    controller.connector.assertCRLF();
    controller.connector.assertChars("% ");

    //
    controller.assertStop();
  }

  public void testCompletion1() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO) {
      @Override
      public Map<String, String> complete(String prefix) {
        return Collections.singletonMap(new StringBuilder(prefix).reverse().toString(), "");
      }
    });
    controller.assertStart();

    //
    controller.connector.append("ab");
    controller.connector.appendTab();
    controller.connector.assertChars("abba");
  }

  public void testCompletion2() throws Exception {
    Controller controller = create(new BaseShell(BaseProcessFactory.ECHO));
    controller.assertStart();

    //
//    controller.connector.append("ab");
//    controller.connector.moveLeft();
//    controller.connector.appendTab();
//    controller.connector.assertChars("ab");
//    controller.connector.assertMoveLeft();
//    controller.connector.assertMoveLeft();

//    controller.connector.assertChars("d");
//    controller.connector.assertMoveLeft();
  }

  public void testCancel() throws Exception {

    final CountDownLatch latch = new CountDownLatch(1);
    Controller controller = create(new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          ShellProcessContext context;
          @Override
          public void process(String request, ShellProcessContext processContext) {
            this.context = processContext;
          }
          @Override
          public void cancel() {
            context.end(new ShellResponse.Display("cancelled"));
            latch.countDown();
          }
        };
      }
    }));

    //
    assertEquals(State.INITIAL, controller.processor.getState());
    Result result1 = controller.processor.execute();
    assertEquals(State.OPEN, result1.getState());
    controller.connector.append("\r\n");
    result1 = controller.processor.execute();
    assertFalse(controller.processor.isAvailable());
    controller.connector.appendBreak();
    Result result2 = controller.processor.execute();
    latch.await();
    assertEquals(State.OPEN, result1.getState());
    assertEquals(State.OPEN, result2.getState());
    assertTrue(controller.processor.isAvailable());
    // controller.connector.assertChars("\r\n");



/*
    assertEquals(Processor.State.WANT_CLOSE, result.getState());
    assertEquals(0, counter.get());
    result = controller.processor.execute();
    assertEquals(Processor.State.CLOSED, result.getState());
    assertEquals(1, counter.get());
*/
  }

  public void testCloseFromInside() throws Exception {

    Controller controller = create(new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            return new ShellResponse.Close();
          }
        };
      }
    }));

    //
    final AtomicInteger counter = new AtomicInteger();
    controller.processor.addListener(new Closeable() {
      public void close() {
        counter.incrementAndGet();
      }
    });

    //
    assertEquals(State.INITIAL, controller.processor.getState());
    Result result = controller.processor.execute();
    assertEquals(State.OPEN, result.getState());
    controller.connector.append("\r\n");
    result = controller.processor.execute();
    assertEquals(State.WANT_CLOSE, result.getState());
    assertEquals(0, counter.get());
    result = controller.processor.execute();
    assertEquals(State.CLOSED, result.getState());
    assertEquals(1, counter.get());
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
      this.processor = new Processor(new BaseTerm(connector), shell);
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
      connector.assertChars("bye");
      connector.assertCRLF();

      // julien : to put back later
/*
      try {
        assertTrue(stopSync.await(4, TimeUnit.SECONDS));
      }
      catch (InterruptedException e) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(e);
        throw afe;
      }

      //
      assertFalse(running);
*/
    }
  }
}
