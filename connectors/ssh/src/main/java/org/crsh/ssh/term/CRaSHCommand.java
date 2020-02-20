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

import org.apache.sshd.server.channel.ChannelSession;
import org.crsh.auth.DisconnectPlugin;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.console.jline.Terminal;
import org.crsh.console.jline.console.ConsoleReader;
import org.apache.sshd.server.Environment;
import org.crsh.console.jline.JLineProcessor;
import org.crsh.shell.Shell;
import org.crsh.auth.AuthInfo;
import org.crsh.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRaSHCommand extends AbstractCommand implements Runnable, Terminal {

  /** . */
  protected static final Logger log = Logger.getLogger(CRaSHCommand.class.getName());

  private static Set<String> unsafeUsers = null;
  private static boolean isInternalSSHConnection = false;
  private static boolean isStandAloneSSHConnection = false;
  private static Object userInfoLock = new Object();

  /** . */
  private final CRaSHCommandFactory factory;

  /** . */
  private Thread thread;

  /** . */
  private String encoding;

  public CRaSHCommand(CRaSHCommandFactory factory) {
    this.factory = factory;
  }

  /** . */
  private SSHContext context;

  /** . */
  private JLineProcessor console;

  public void start(ChannelSession channel, Environment env) throws IOException {
    context = new SSHContext(env);
    encoding = context.encoding != null ? context.encoding.name() : factory.encoding.name();
    thread = new Thread(this, "CRaSH");

    //
    thread.start();
  }

  public SSHContext getContext() {
    return context;
  }

  // Called only for SSH clients using interactive shell.
  public void destroy(ChannelSession channel) {
    Utils.close(console);
    DisconnectPlugin disconnectHandler = factory.pluginContext.getPlugin(DisconnectPlugin.class);
    if (disconnectHandler != null) {
      final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      AuthInfo authInfo = session.getAttribute(SSHLifeCycle.AUTH_INFO);
      disconnectHandler.onDisconnect(userName, authInfo);
      log.info("Session " + userName + "@" + session.getIoSession().getRemoteAddress() + " disconnected");
    }
    thread.interrupt();
  }

  public void run() {
    final AtomicBoolean exited = new AtomicBoolean(false);
    try {
      final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      AuthInfo authInfo = session.getAttribute(SSHLifeCycle.AUTH_INFO);
      Principal user = new Principal() {
        public String getName() {
          return userName;
        }
      };

      boolean safeUser = !isUserUnsafe(user.getName());

      ShellSafety shellSafety = ShellSafetyFactory.getCurrentThreadShellSafety();
      shellSafety.setSafeShell(safeUser);
      shellSafety.setInternal(isInternalSSH());
      shellSafety.setSSH(true);
      shellSafety.setStandAlone(isStandAloneSSH());
      Shell shell = factory.shellFactory.create(user, authInfo, shellSafety);
      ConsoleReader reader = new ConsoleReader(in, out, this) {
        @Override
        public void shutdown() {
          exited.set(true);
          callback.onExit(0);
          super.shutdown();
        }
      };
      JLineProcessor processor = new JLineProcessor(true, shell, reader, new PrintStream(out, false, encoding), "\r\n");
      processor.run();
    } catch (java.io.InterruptedIOException e) {
      // Expected behavior because of the onExit callback in the shutdown above
      // clear interrupted status on purpose
      Thread.interrupted();
    } catch (Exception e) {
      log.log(Level.WARNING, "Error during execution", e);
    } finally {
      // Make sure we call it
      if (!exited.get()) {
        callback.onExit(0);
      }
    }
  }

  //

  @Override
  public String getOutputEncoding() {
    return encoding;
  }

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

  @Override
  public void disableInterruptCharacter() {

  }

  @Override
  public void enableInterruptCharacter() {

  }

  public static void setUserInfo(Set<String> users, boolean internalSSHShell, boolean standaloneSSHShell) {
    synchronized (userInfoLock) {
      unsafeUsers = users;
      isInternalSSHConnection = internalSSHShell;
      isStandAloneSSHConnection = standaloneSSHShell;
    }
  }

  public static boolean isUserUnsafe(String username) {
    synchronized (userInfoLock) {
      return unsafeUsers != null && unsafeUsers.contains(username);
    }
  }

  public static boolean isInternalSSH() {
    synchronized (userInfoLock) {
    return isInternalSSHConnection;
    }
  }

  public static boolean isStandAloneSSH() {
    synchronized (userInfoLock) {
      return isStandAloneSSHConnection;
    }
  }
}


