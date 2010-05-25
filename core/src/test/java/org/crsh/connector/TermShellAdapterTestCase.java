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

package org.crsh.connector;

import junit.framework.TestCase;
import org.crsh.shell.Connector;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TermShellAdapterTestCase extends TestCase {

  public void testReadLine() throws Exception {
//    testReadLine(true);
  }

/*
  public void testReadLineNoExecutor() throws Exception {
    testReadLine(false);
  }
*/

  private void testReadLine(boolean withExecutor) throws Exception {

    TestTerm term = new TestTerm();
    TestShell shell = new TestShell();
    Connector connector;
    if (withExecutor) {
      connector = new Connector(Executors.newSingleThreadExecutor(), shell);
    } else {
      connector = new Connector(shell);
    }
    final TermShellAdapter adapter = new TermShellAdapter(term, connector);

    //
    assertEquals(TermStatus.SHUTDOWN, adapter.getStatus());

    //
    new Thread() {
      @Override
      public void run() {
        try {
          adapter.run();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();

    // Wait until ready
    while (adapter.getStatus() != TermStatus.READY) {
      Thread.sleep(10);
    }

    //
    term.add(new TermAction.ReadLine("foo"));

    //
    shell.append(new TestShellAction() {
      public ShellResponse evaluate(String request, ShellResponseContext responseContext) throws Exception {
        String resp = responseContext.readLine("bar");
        return new ShellResponse.Display(resp);
      }
    });

    // Wait until it wants to read input
    while (adapter.getStatus() != TermStatus.READING_INPUT) {
      Thread.sleep(10);
    }

    // Send a line
    term.add(new TermAction.ReadLine("juu"));

    // Wait until it is ready again
    while (adapter.getStatus() != TermStatus.READY) {
      Thread.sleep(10);
    }

    System.out.println("ABC");

    // Say bye
    term.add(new TermAction.ReadLine("bye"));

    // Wait until it is shutdown
    while (adapter.getStatus() != TermStatus.SHUTDOWN) {
      Thread.sleep(10);
    }

    // Now we should be able to obtain the response
    System.out.println("term.getOutput() = " + term.getOutput());
  }

}
