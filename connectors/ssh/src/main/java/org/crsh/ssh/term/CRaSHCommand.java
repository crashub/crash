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
package org.crsh.ssh.term;

import jline.Terminal;
import jline.console.ConsoleReader;
import org.apache.sshd.server.Environment;
import org.crsh.console.jline.JLineProcessor;
import org.crsh.shell.Shell;
import org.crsh.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Principal;

public class CRaSHCommand extends AbstractCommand implements Runnable, Terminal {

  /** . */
  private final CRaSHCommandFactory factory;

  /** . */
  private Thread thread;

  public CRaSHCommand(CRaSHCommandFactory factory) {
    this.factory = factory;
  }

  /** . */
  private SSHContext context;

  /** . */
  private JLineProcessor console;

  public void start(Environment env) throws IOException {

    //
    context = new SSHContext(env);

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  public SSHContext getContext() {
    return context;
  }

  public void destroy() {
    Utils.close(console);
    thread.interrupt();
  }

  public void run() {
    try {
      final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      Principal user = new Principal() {
        public String getName() {
          return userName;
        }
      };
      Shell shell = factory.shellFactory.create(user);
      ConsoleReader reader = new ConsoleReader(in, out, this);
      JLineProcessor processor = new JLineProcessor(shell, reader, new PrintStream(out), "\r\n");
      processor.run();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      callback.onExit(0);
    }
  }

  //

  @Override
  public void init() throws Exception {
  }

  @Override
  public void restore() throws Exception {
  }

  @Override
  public void reset() throws Exception {
  }

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public int getWidth() {
    return context.getWidth();
  }

  @Override
  public int getHeight() {
    return context.getHeight();
  }

  @Override
  public boolean isAnsiSupported() {
    return true;
  }

  @Override
  public OutputStream wrapOutIfNeeded(OutputStream out) {
    return out;
  }

  @Override
  public InputStream wrapInIfNeeded(InputStream in) throws IOException {
    return in;
  }

  @Override
  public boolean hasWeirdWrap() {
    return false;
  }

  @Override
  public boolean isEchoEnabled() {
    return false;
  }

  @Override
  public void setEchoEnabled(boolean enabled) {
  }
}


