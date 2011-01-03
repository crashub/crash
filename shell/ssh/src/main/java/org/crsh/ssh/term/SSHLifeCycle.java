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
package org.crsh.ssh.term;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginManager;
import org.crsh.ssh.term.scp.CommandPlugin;
import org.crsh.ssh.term.scp.SCPCommandFactory;
import org.crsh.term.CRaSHLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHLifeCycle extends CRaSHLifeCycle {

  /** . */
  public static final Session.AttributeKey<String> USERNAME = new Session.AttributeKey<java.lang.String>();

  /** . */
  public static final Session.AttributeKey<String> PASSWORD = new Session.AttributeKey<java.lang.String>();

  /** . */
  private final Logger log = LoggerFactory.getLogger(SSHLifeCycle.class);

  /** . */
  private SshServer server;

  /** . */
  private int port;

  /** . */
  private String keyPath;

  public SSHLifeCycle(PluginContext context) {
    super(context);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getKeyPath() {
    return keyPath;
  }

  public void setKeyPath(String keyPath) {
    this.keyPath = keyPath;
  }

  @Override
  protected void doInit() {
    try {
      PluginManager<CommandPlugin> commandPlugins = new PluginManager<CommandPlugin>(getShellContext(), CommandPlugin.class);

      //
      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(port);
      server.setShellFactory(new CRaSHCommandFactory(getShellFactory(), getExecutor()));
      server.setCommandFactory(new SCPCommandFactory(commandPlugins));
      server.setKeyPairProvider(new PEMGeneratorHostKeyProvider(keyPath));

      //
      server.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String _username, String _password, ServerSession session) {
          session.setAttribute(USERNAME, _username);
          session.setAttribute(PASSWORD, _password);
          return true;
        }
      });

      //
      log.info("About to start CRaSSHD");
      server.start();
      log.info("CRaSSHD started on port " + port);

      //
      this.server = server;
    }
    catch (Throwable e) {
      log.error("Could not start CRaSSHD", e);
    }
  }

  @Override
  protected void doDestroy() {
    if (server != null) {
      try {
        server.stop();
      }
      catch (InterruptedException e) {
        log.debug("Got an interruption when stopping server", e);
      }
    }
  }
}
