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

package org.crsh.shell.impl.async;

import com.google.common.util.concurrent.MoreExecutors;
import org.crsh.AbstractTestCase;
import test.shell.base.BaseProcess;
import test.shell.base.BaseProcessContext;
import test.shell.base.BaseProcessFactory;
import test.shell.base.BaseShell;
import test.plugin.TestPluginLifeCycle;
import org.crsh.shell.*;

import java.io.IOException;
import java.util.concurrent.Executors;

public class AsyncShellTestCase extends AbstractTestCase {

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
          public void process(String request, ShellProcessContext processContext) throws IOException {
            String a = null;
            try {
              a = readLine("bar", true);
            }
            catch (InterruptedException e) {
            }
            processContext.append(a);
            processContext.end(ShellResponse.ok());
          }
        };
      }
    };

    //
    Shell shell = new BaseShell(factory);

    //
    Shell asyncShell = new AsyncShell(Executors.newSingleThreadExecutor(), shell);

    //
    BaseProcessContext ctx = BaseProcessContext.create(asyncShell, "foo");
    ctx.addLineInput("juu");
    ctx.execute();

    //
    ShellResponse resp = ctx.getResponse();

    //
    assertInstance(ShellResponse.Ok.class, resp);
    assertEquals("barjuu", ctx.getOutput());
    ctx.assertNoInput();
  }

  public void testAsyncEvaluation() throws InterruptedException {
    AsyncShell connector = new AsyncShell(Executors.newSingleThreadExecutor(), lifeCycle.createShell());
    status = 0;
    BaseProcessContext ctx = BaseProcessContext.create(connector, "invoke " + AsyncShellTestCase.class.getName() + " bilto");
    ctx.execute();
    ShellResponse resp = ctx.getResponse();
    assertTrue("Was not expecting response to be " + resp.getMessage(), resp instanceof ShellResponse.Ok);
    assertEquals(1, status);
    ctx.getResponse();
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
    AsyncShell  asyncShell = new AsyncShell(MoreExecutors.sameThreadExecutor(), shell);

    //
    BaseProcessContext ctx = BaseProcessContext.create(asyncShell, "hello").execute();
    assertEquals(Status.TERMINATED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertInstance(ShellResponse.Ok.class, ctx.getResponse());
    assertEquals("hello", ctx.getOutput());
  }
}
