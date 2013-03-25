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

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.crsh.plugin.PluginContext;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.ssh.term.scp.SCPCommandFactory;
import org.crsh.term.TermLifeCycle;
import org.crsh.term.spi.TermIOHandler;
import org.crsh.vfs.Resource;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHLifeCycle extends TermLifeCycle {

  /** . */
  public static final Session.AttributeKey<String> USERNAME = new Session.AttributeKey<java.lang.String>();

  /** . */
  public static final Session.AttributeKey<String> PASSWORD = new Session.AttributeKey<java.lang.String>();

  /** . */
  private final Logger log = Logger.getLogger(SSHLifeCycle.class.getName());

  /** . */
  private SshServer server;

  /** . */
  private int port;

  /** . */
  private Resource key;

  /** . */
  private String authentication;

  /** . */
  private Integer localPort;

  public SSHLifeCycle(PluginContext context) {
    super(context);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Returns the local part after the ssh server has been succesfully bound or null. This is useful when
   * the port is chosen at random by the system.
   *
   * @return the local port
   */
  public Integer getLocalPort() {
	  return localPort;
  }
  
  public Resource getKey() {
    return key;
  }

  public void setKey(Resource key) {
    this.key = key;
  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }

  @Override
  protected void doInit() {
    try {

      //
      TermIOHandler handler = getHandler();

      //
      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(port);
      server.setShellFactory(new CRaSHCommandFactory(handler));
      server.setCommandFactory(new SCPCommandFactory(getContext()));
      server.setKeyPairProvider(new URLKeyPairProvider(key));

      // We never authenticate by default
      AuthenticationPlugin plugin = new AuthenticationPlugin() {
        public String getName() {
          return "null";
        }
        public boolean authenticate(String username, String password) throws Exception {
          return false;
        }
      };

      // Lookup for an authentication plugin
      if (authentication != null) {
        for (AuthenticationPlugin authenticationPlugin : getContext().getPlugins(AuthenticationPlugin.class)) {
          if (authentication.equals(authenticationPlugin.getName())) {
            plugin = authenticationPlugin;
            break;
          }
        }
      }

      //
      final AuthenticationPlugin authPlugin = plugin;

      //
      server.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String _username, String _password, ServerSession session) {
          boolean auth;
          try {
            log.log(Level.FINE, "Using authentication plugin " + authPlugin + " to authenticate user " + _username);
            auth = authPlugin.authenticate(_username, _password);
          } catch (Exception e) {
            log.log(Level.SEVERE, "Exception authenticating user " + _username + " in authentication plugin: " + authPlugin, e);
            return false;
          }

          // We store username and password in session for later reuse
          session.setAttribute(USERNAME, _username);
          session.setAttribute(PASSWORD, _password);

          //
          return auth;
        }
      });

      //
      log.log(Level.INFO, "About to start CRaSSHD");
      server.start();
      localPort = server.getPort();
      log.log(Level.INFO, "CRaSSHD started on port " + localPort);

      //
      this.server = server;
    }
    catch (Throwable e) {
      log.log(Level.SEVERE, "Could not start CRaSSHD", e);
    }
  }

  @Override
  protected void doDestroy() {
    if (server != null) {
      try {
        server.stop();
      }
      catch (InterruptedException e) {
        log.log(Level.FINE, "Got an interruption when stopping server", e);
      }
    }
  }
}
