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

package org.crsh.term;

import junit.framework.TestCase;
import org.crsh.BaseProcess;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.TestPluginContext;
import org.crsh.shell.*;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.shell.concurrent.Status;
import org.crsh.shell.concurrent.SyncShellResponseContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AsyncShellTestCase extends TestCase {

  /** . */
  protected ShellFactory builder;

  /** . */
  protected TestPluginContext context;

  /** . */
  protected ExecutorService executor;

  /** . */
  private static volatile int status;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    executor = Executors.newSingleThreadExecutor();
    context = new TestPluginContext(
    );
    builder = new ShellFactory(context);
  }

  public void testReadLine() throws Exception {
    final LinkedList<String> output = new LinkedList<String>();
    final LinkedList<String> input = new LinkedList<String>();
    input.add("juu");

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
    Shell asyncShell = new AsyncShell(executor, shell);

    //
    ShellProcessContext respCtx1 = new ShellProcessContext() {
      public int getWidth() {
        return 32;
      }
      public void begin(ShellProcess process) {
      }
      public String readLine(String msg, boolean echo) {
        output.addLast(msg);
        return input.isEmpty() ? null : input.removeLast();
      }
      public void end(ShellResponse response) {
      }
    };
    SyncShellResponseContext respCtx2 = new SyncShellResponseContext(respCtx1);
    asyncShell.createProcess("foo").execute(respCtx2);

    //
    ShellResponse resp = respCtx2.getResponse();

    //
    assertTrue(resp instanceof ShellResponse.Display);
    assertEquals("juu", resp.getText());
    assertEquals(Collections.singletonList("bar"), output);
    assertEquals(0, input.size());
  }

  public void testCancelEvaluation() throws InterruptedException {

    //
    final ShellResponse ok = new ShellResponse.Ok();
    final AtomicBoolean fail = new AtomicBoolean(false);
    final AtomicInteger status = new AtomicInteger(0);
    final AtomicInteger cancelled = new AtomicInteger(0);

    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {

            //
            fail.set(!"foo".equals(request));

            //
            if (status.get() == 0) {
              status.set(1);
              int r = status.get();
              while (r == 1) {
                r = status.get();
              }
              if (r == 2) {
                status.set(3);
              } else {
                status.set(-1);
              }
            } else {
              status.set(-1);
            }

            //
            return ok;
          }
          @Override
          public void cancel() {
            cancelled.getAndIncrement();
          }
        };
      }
    };

    /** . */
    Shell shell = new BaseShell(factory);

    //
    AsyncShell  asyncShell = new AsyncShell(executor, shell);

    //
    SyncShellResponseContext respCtx = new SyncShellResponseContext();
    ShellProcess process = asyncShell.createProcess("foo");
    process.execute(respCtx);
    assertEquals(Status.EVALUATING, asyncShell.getStatus());
    assertEquals(0, cancelled.get());

    //
    int r;
    for (r = status.get();r == 0;r = status.get()) { }
    assertEquals(1, r);
    assertEquals(Status.EVALUATING, asyncShell.getStatus());
    assertEquals(0, cancelled.get());

    //
    process.cancel();
    assertEquals(Status.CANCELED, asyncShell.getStatus());
    assertEquals(1, cancelled.get());

    //
    process.cancel();
    assertEquals(Status.CANCELED, asyncShell.getStatus());
    assertEquals(1, cancelled.get());

    //
    status.set(2);
    for (r = status.get();r == 2;r = status.get()) { }
    assertEquals(3, r);
    respCtx.getResponse();
    assertEquals(Status.AVAILABLE, asyncShell.getStatus());
    assertEquals(1, cancelled.get());

    //
    assertFalse(fail.get());
  }

  public void testAsyncEvaluation() throws InterruptedException {
    AsyncShell connector = new AsyncShell(executor, builder.build());
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
}
