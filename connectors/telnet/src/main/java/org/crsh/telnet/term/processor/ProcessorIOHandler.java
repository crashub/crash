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

package org.crsh.telnet.term.processor;

import org.crsh.auth.AuthInfo;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.telnet.term.console.ConsoleTerm;
import org.crsh.telnet.term.spi.TermIO;
import org.crsh.telnet.term.spi.TermIOHandler;

import java.security.Principal;

public class ProcessorIOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private ShellFactory factory;

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  @Override
  public void init() {
    this.factory = getContext().getPlugin(ShellFactory.class);
  }

  @Override
  public void destroy() {
  }

  public void handle(final TermIO io, Principal user, AuthInfo authInfo) {
    Shell shell = factory.create(user, authInfo, ShellSafetyFactory.getCurrentThreadShellSafety());
    ConsoleTerm term = new ConsoleTerm(io);
    Processor processor = new Processor(term, shell);
    processor.addListener(io);
    processor.addListener(shell);
    processor.run();
  }
}