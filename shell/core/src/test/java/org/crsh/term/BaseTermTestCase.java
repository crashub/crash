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

package org.crsh.term;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.term.processor.TermProcessor;
import org.crsh.term.processor.TermResponseContext;
import org.crsh.term.spi.TestTermConnector;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseTermTestCase extends TestCase {

  private static final TermProcessor ECHO_PROCESSOR = new TermProcessor() {
    public boolean process(TermAction action, TermResponseContext responseContext) {
      if (action instanceof TermAction.Init) {
        responseContext.done(false);
        return true;
      } else if (action instanceof TermAction.ReadLine) {
        String line = ((TermAction.ReadLine)action).getLine();
        if ("bye".equals(line)) {
          responseContext.done(true);
          return true;
        } else {
          try {
            responseContext.write(line);
            responseContext.done(false);
            return true;
          }
          catch (IOException e) {
            return false;
          }
        }
      } else {
        responseContext.done(true);
        return true;
      }
    }
  };



  public void testLine() throws Exception {
    Controller controller = create(ECHO_PROCESSOR);
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
    Controller controller = create(ECHO_PROCESSOR);
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


  private Controller create(TermProcessor processor) throws IOException {
    return new Controller(new TestTermConnector(), processor);
  }

  private class Controller extends BaseTerm {

    /** . */
    private volatile boolean running;

    /** . */
    private final CountDownLatch startSync;

    /** . */
    private final CountDownLatch stopSync;

    /** . */
    private final Thread thread;

    /** . */
    private final TestTermConnector connector;

    private Controller(TestTermConnector connector, TermProcessor processor) {
      super(connector, processor);

      //
      this.running = true;
      this.startSync = new CountDownLatch(1);
      this.stopSync = new CountDownLatch(1);
      this.thread = new Thread(this);
      this.connector = connector;
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
      assertEquals(running, true);

      //
      connector.assertCRLF();
      connector.assertChars("% ");
    }

    @Override
    public void run() {
      running = true;
      startSync.countDown();
      try {
        super.run();
      }
      finally {
        running = false;
        stopSync.countDown();
      }
    }

    public void assertStop() {
      assertEquals(running, true);

      //
      connector.append("bye\r\n");
      connector.assertChars("bye");
      connector.assertCRLF();

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
      assertEquals(running, false);
    }
  }
}
