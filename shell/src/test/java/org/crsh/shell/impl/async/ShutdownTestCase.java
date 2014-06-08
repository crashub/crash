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

import org.crsh.AbstractTestCase;
import test.shell.base.BaseProcess;
import test.shell.base.BaseProcessContext;
import test.shell.base.BaseProcessFactory;
import test.shell.base.BaseShell;
import test.CommandQueue;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class ShutdownTestCase extends AbstractTestCase {

  public void testCreate() throws Exception {
    Shell shell = new BaseShell();
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);
    asyncShell.close();
    try {
      asyncShell.createProcess("foo");
    }
    catch (IllegalStateException e) {
    }
  }

  public void testExecute() throws Exception {
    Shell shell = new BaseShell();
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);
    BaseProcessContext ctx = BaseProcessContext.create(asyncShell, "foo");
    asyncShell.close();
    ctx.execute();
    assertEquals(Status.TERMINATED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(ShellResponse.Cancelled.class, ctx.getResponse().getClass());
    assertEquals(0, commands.getSize());
  }

  public void testCancel() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    final CountDownLatch latch1 = new CountDownLatch(1);
    final CountDownLatch latch2 = new CountDownLatch(1);

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            latch1.countDown();
            try {
              latch2.await();
            } catch (InterruptedException e) {
              failure.set(e);
            }
            return ShellResponse.ok();
          }
          @Override
          public void cancel() {
            latch2.countDown();
          }
        };
      }
    };

    //
    Shell shell = new BaseShell(factory);
    CommandQueue commands = new CommandQueue();
    AsyncShell  asyncShell = new AsyncShell(commands, shell);

    //
    BaseProcessContext ctx = BaseProcessContext.create(asyncShell, "foo").execute();
    Future<?> future = commands.executeAsync();
    latch1.await();
    assertEquals(Status.EVALUATING, ((AsyncProcess)ctx.getProcess()).getStatus());

    //
    asyncShell.close();
    assertEquals(Status.CANCELED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(ShellResponse.Cancelled.class, ctx.getResponse().getClass());
  }
}
