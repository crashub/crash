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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CancellationTestCase extends AbstractTestCase {


  public void testEvaluating() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    final AtomicInteger cancelCount = new AtomicInteger(0);
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
            cancelCount.getAndIncrement();
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
    assertEquals(Status.QUEUED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(0, cancelCount.get());
    assertEquals(1, commands.getSize());

    // Execute the command
    // And wait until the other thread is waiting
    Future<?> future = commands.executeAsync();
    latch1.await();
    assertEquals(Status.EVALUATING, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(0, cancelCount.get());

    //
    ctx.getProcess().cancel();
    assertEquals(Status.CANCELED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(1, cancelCount.get());

    //
    ctx.getProcess().cancel();
    assertEquals(Status.CANCELED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(1, cancelCount.get());

    // Wait until it's done
    latch2.countDown();
    future.get();

    // Test we received a cancelled response even though we provided an OK result
    assertEquals(ShellResponse.Cancelled.class, ctx.getResponse().getClass());
    assertEquals(Status.TERMINATED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(1, cancelCount.get());

    //
    safeFail(failure.get());
  }

  public void testQueued() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            failure.set(failure("Was not exepecting request"));
            return ShellResponse.ok();
          }
          @Override
          public void cancel() {
            failure.set(failure("Was not exepecting cancel"));
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
    assertEquals(Status.QUEUED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(1, commands.getSize());

    //
    ctx.getProcess().cancel();
    assertEquals(Status.CANCELED, ((AsyncProcess)ctx.getProcess()).getStatus());

    // Execute the command
    Future<?> future = commands.executeAsync();
    future.get();

    // Test we get terminated status and the callback was done
    assertEquals(Status.TERMINATED, ((AsyncProcess)ctx.getProcess()).getStatus());
    assertEquals(ShellResponse.Cancelled.class, ctx.getResponse().getClass());
    safeFail(failure.get());
  }
}
