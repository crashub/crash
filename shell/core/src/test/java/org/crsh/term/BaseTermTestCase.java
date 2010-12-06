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
import junit.framework.TestCase;
import org.crsh.term.processor.TermProcessor;
import org.crsh.term.processor.TermResponseContext;
import org.crsh.term.spi.TestTermConnector;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseTermTestCase extends TestCase {

  public void testFoo() throws Exception {

    TestTermConnector connector = new TestTermConnector();

    BaseTerm baseTerm = new BaseTerm(connector, new TermProcessor() {
      public boolean process(TermAction action, TermResponseContext responseContext) {
        if (action instanceof TermAction.Init) {
          responseContext.done(false);
          return true;
        } else if (action instanceof TermAction.ReadLine) {
          try {
            responseContext.write(((TermAction.ReadLine)action).getLine());
            responseContext.done(false);
            return true;
          }
          catch (IOException e) {
            return false;
          }
        } else {
          responseContext.done(true);
          return true;
        }
      }
    });

    TermController controller = run(baseTerm);

    controller.assertRunning();

    //
    connector.assertCRLF();
    connector.assertChars("% ");

    //
    connector.append("abc\r\n");
    connector.assertChars("abc");
    connector.assertCRLF();
    connector.assertChars("abc");
    connector.assertCRLF();
    connector.assertChars("% ");

/*
    //
    connector.append("abc");
    connector.appendDel();
    connector.append("\r\n");
    connector.assertChars("abc");
    connector.assertDel();
    connector.assertCRLF();
    connector.assertChars("ab");
    connector.assertCRLF();
    connector.assertChars("% ");
*/


  }

  private TermController run(BaseTerm term) {
    TermController controller = new TermController(term);
    controller.start();
    return controller;
  }

  private class TermController extends Thread {

    /** . */
    private final BaseTerm term;

    /** . */
    private volatile boolean running;

    private TermController(BaseTerm term) {
      this.term = term;
      this.running = true;
    }

    @Override
    public void run() {
      try {
        term.run();
      }
      finally {
        running = false;
      }
    }

    public void assertRunning() {
      Assert.assertTrue(running);
    }
  }

}
