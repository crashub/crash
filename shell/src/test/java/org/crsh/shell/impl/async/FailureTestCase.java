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

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FailureTestCase extends AbstractTestCase {

  public void testEvaluating() throws Exception {
    final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    final AtomicInteger cancelCount = new AtomicInteger(0);

    //
    BaseProcessFactory factory = new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            throw new RuntimeException();
          }
          @Override
          public void cancel() {
            failure.set(failure("Was expecting no cancel callback"));
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
    future.get();

    //
    assertEquals(ShellResponse.Error.class, ctx.getResponse().getClass());
  }
}
