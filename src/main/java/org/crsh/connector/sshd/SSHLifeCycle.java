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
package org.crsh.connector.sshd;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.crsh.connector.CRaSHLifeCycle;
import org.crsh.connector.sshd.scp.SCPCommandFactory;
import org.crsh.shell.ShellContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.security.PublicKey;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHLifeCycle extends CRaSHLifeCycle {

  /** . */
  private final Logger log = LoggerFactory.getLogger(SSHLifeCycle.class);

  /** . */
  private SshServer server;

  /** . */
  private String userName;

  /** . */
  private String password;

  /** . */
  private int port;

  /** . */
  private String keyPath;

  public SSHLifeCycle(ShellContext context) {
    super(context);
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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
      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(port);
      server.setShellFactory(new CRaSHCommandFactory(getShellBuilder()));
      server.setCommandFactory(new SCPCommandFactory());
      server.setKeyPairProvider(new PEMGeneratorHostKeyProvider(keyPath));

      // No idea if I should use something different than that
      server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
        public boolean authenticate(String username, PublicKey key, ServerSession session) {
          return true;
        }
      });

      // For now authenticates in a very simply manner from web.xml setting
      server.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String _username, String _password, ServerSession session) {
          return userName.equals(_username) && password.equals(_password);
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
