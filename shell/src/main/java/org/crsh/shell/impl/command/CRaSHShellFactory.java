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

package org.crsh.shell.impl.command;

import org.crsh.auth.AuthInfo;
import org.crsh.command.ShellSafety;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.impl.async.AsyncShell;

import java.security.Principal;

public class CRaSHShellFactory extends CRaSHPlugin<ShellFactory> implements ShellFactory {

  /** . */
  private CRaSH crash;
  //++++private boolean safeMode = false; //++++add

  public CRaSHShellFactory() {
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    System.out.println("CRaSHShellFactory: NEW CRASH++++ via Init()++++"); //++++
    crash = new CRaSH(context); //++++add
  }

  @Override
  public ShellFactory getImplementation() {
    return this;
  }

  public Shell create(Principal principal, boolean async, AuthInfo authInfo, ShellSafety shellSafety) { //++++KEEP
    CRaSHSession session = crash.createSession(principal, authInfo, shellSafety);
    if (async) {
      return new AsyncShell(getContext().getExecutor(), session);
    } else {
      return session;
    }
  }

  @Override
  public Shell create(Principal principal, AuthInfo authInfo, ShellSafety shellSafety) { //++++KEEP
    return create(principal, true, authInfo, shellSafety);
  }
}
