/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.shell.concurrent;

import junit.framework.TestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.TestPluginLifeCycle;
import org.crsh.shell.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AsyncShellTestCase extends TestCase {

  /** . */
  protected TestPluginLifeCycle lifeCycle;

  /** . */
  private static volatile int status;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    TestPluginLifeCycle lifeCycle = new TestPluginLifeCycle();
    lifeCycle.start();

    //
    this.lifeCycle = lifeCycle;
  }

  public void testReadLine() throws Exception {

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            String a = readLine("bar", true);
            return new ShellResponse.Display(a);
          }
        };
      }
    };

    //
    Shell shell = new BaseShell(factory);

    //
    Shell asyncShell = new AsyncShell(Executors.newSingleThreadExecutor(), shell);

    //
    SyncShellResponseContext ctx = new SyncShellResponseContext();
    ctx.addLineInput("juu");
    asyncShell.createProcess("foo").execute(ctx);

    //
    ShellResponse resp = ctx.getResponse();

    //
    assertTrue(resp instanceof ShellResponse.Display);
    assertEquals("juu", resp.getText());
    ctx.assertLineOutput("bar");
    ctx.assertNoOutput();
    ctx.assertNoInput();
  }

  public void testAsyncEvaluation() throws InterruptedException {
    AsyncShell connector = new AsyncShell(Executors.newSingleThreadExecutor(), lifeCycle.createShell());
    status = 0;
    SyncShellResponseContext respCtx = new SyncShellResponseContext();
    connector.createProcess("invoke " + AsyncShellTestCase.class.getName() + " bilto").execute(respCtx);
    ShellResponse resp = respCtx.getResponse();
    assertTrue("Was not expecting response to be " + resp.getText(), resp instanceof ShellResponse.Ok);
    assertEquals(1, status);
    respCtx.getResponse();
  }

  public static void bilto() {
    if (status == 0) {
      status = 1;
    } else {
      status = -1;
    }
  }

  public void testDirect() throws Exception {
    Shell shell = new BaseShell(BaseProcessFactory.ECHO);
    AsyncShell  asyncShell = new AsyncShell(new Executor() {
      public void execute(Runnable command) {
        command.run();
      }
    }, shell);

    //
    AsyncProcess process = asyncShell.createProcess("hello");
    SyncShellResponseContext ctx = new SyncShellResponseContext();
    process.execute(ctx);
    assertEquals(Status.TERMINATED, process.getStatus());
    assertEquals(new ShellResponse.Display("hello"), ctx.getResponse());
  }
}
