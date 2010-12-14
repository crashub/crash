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
import org.crsh.TestShell;
import org.crsh.TestShellContext;
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
  protected TestShellContext context;

  /** . */
  protected ExecutorService executor;

  /** . */
  private static volatile int status;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    executor = Executors.newSingleThreadExecutor();
    context = new TestShellContext(
      "groovy/commands/base/",
      "groovy/commands/jcr/",
      "groovy/commands/test/");
    builder = new ShellFactory(context);
  }

  public void testReadLine() throws Exception {
    final LinkedList<String> output = new LinkedList<String>();
    final LinkedList<String> input = new LinkedList<String>();
    input.add("juu");

    //
    Shell shell = new TestShell() {
      @Override
      public void process(String request, ShellProcessContext processContext) {
        String a = processContext.readLine("bar", true);
        processContext.done(new ShellResponse.Display(a));
      }
    };

    //
    AsyncShell connector = new AsyncShell(executor, shell);
    connector.open();

    //
    ShellProcessContext respCtx1 = new ShellProcessContext() {
      public String readLine(String msg, boolean echo) {
        output.addLast(msg);
        return input.isEmpty() ? null : input.removeLast();
      }
      public void done(ShellResponse response) {
      }
    };
    SyncShellResponseContext respCtx2 = new SyncShellResponseContext(respCtx1);
    connector.process("foo", respCtx2);

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

    //
    Shell shell = new TestShell() {
      @Override
      public void process(String request, ShellProcessContext processContext) {

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
        processContext.done(ok);
      }
    };

    //
    AsyncShell  connector = new AsyncShell(executor, shell);
    connector.open();

    //
    SyncShellResponseContext respCtx = new SyncShellResponseContext();
    connector.process("foo", respCtx);
    assertEquals(Status.EVALUATING, connector.getStatus());

    //
    int r;
    for (r = status.get();r == 0;r = status.get()) { }
    assertEquals(1, r);
    assertEquals(Status.EVALUATING, connector.getStatus());

    //
    assertTrue(connector.cancel());
    assertEquals(Status.CANCELED, connector.getStatus());

    //
    status.set(2);
    for (r = status.get();r == 2;r = status.get()) { }
    assertEquals(3, r);
    respCtx.getResponse();
    assertEquals(Status.AVAILABLE, connector.getStatus());

    //
    assertFalse(fail.get());
  }

  public void testAsyncEvaluation() throws InterruptedException {
    AsyncShell connector = new AsyncShell(executor, builder.build());
    connector.open();
    status = 0;
    SyncShellResponseContext respCtx = new SyncShellResponseContext();
    connector.process("invoke " + AsyncShellTestCase.class.getName() + " bilto", respCtx);
    while (status == 0) {
      // Do nothing
    }
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
