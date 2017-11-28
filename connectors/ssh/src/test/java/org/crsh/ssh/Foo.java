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
package org.crsh.ssh;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.auth.AuthInfo;
import test.shell.sync.SyncShell;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.CountDownLatch;

/**
 * @author Julien Viet
 */
public class Foo extends CRaSHPlugin<ShellFactory> {

  /** . */
  final CountDownLatch closed = new CountDownLatch(1);

  /** . */
  final SyncShell shell = new SyncShell() {
    @Override
    public void close() throws IOException {
      closed.countDown();
    }
  };

  @Override
  public ShellFactory getImplementation() {
    return new ShellFactory() {
      @Override
      public Shell create(Principal principal, AuthInfo authInfo) {
        return shell;
      }
    };
  }
}
