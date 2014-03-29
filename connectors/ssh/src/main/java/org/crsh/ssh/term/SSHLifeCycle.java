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
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.session.ServerSession;
import org.crsh.plugin.PluginContext;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.ssh.term.scp.SCPCommandFactory;
import org.crsh.ssh.term.subsystem.SubsystemFactoryPlugin;
import org.crsh.term.TermLifeCycle;
import org.crsh.term.spi.TermIOHandler;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interesting stuff here : http://gerrit.googlecode.com/git-history/4b9e5e7fb9380cfadd28d7ffe3dc496dc06f5892/gerrit-sshd/src/main/java/com/google/gerrit/sshd/DatabasePubKeyAuth.java
 */
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
  private int idleTimeout;

  /** . */
  private int authTimeout;


  /** . */
  private KeyPairProvider keyPairProvider;

  /** . */
  private final ArrayList<AuthenticationPlugin> authenticationPlugins;

  /** . */
  private Integer localPort;

  public SSHLifeCycle(PluginContext context, ArrayList<AuthenticationPlugin> authenticationPlugins) {
    super(context);

    //
    this.authenticationPlugins = authenticationPlugins;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }


  public int getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public int getAuthTimeout() {
    return authTimeout;
  }

  public void setAuthTimeout(int authTimeout) {
    this.authTimeout = authTimeout;
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
  
  public KeyPairProvider getKeyPairProvider() {
    return keyPairProvider;
  }

  public void setKeyPairProvider(KeyPairProvider keyPairProvider) {
    this.keyPairProvider = keyPairProvider;
  }

  @Override
  protected void doInit() {
    try {

      //
      TermIOHandler handler = getHandler();

      //
      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(port);

      if (this.idleTimeout > 0) {
        server.getProperties().put(ServerFactoryManager.IDLE_TIMEOUT, String.valueOf(this.idleTimeout));
      }
      if (this.authTimeout > 0) {
        server.getProperties().put(ServerFactoryManager.AUTH_TIMEOUT, String.valueOf(this.authTimeout));
      }

      server.setShellFactory(new CRaSHCommandFactory(handler));
      server.setCommandFactory(new SCPCommandFactory(getContext()));
      server.setKeyPairProvider(keyPairProvider);

      //
      ArrayList<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>(0);
      for (SubsystemFactoryPlugin plugin : getContext().getPlugins(SubsystemFactoryPlugin.class)) {
        namedFactoryList.add(plugin.getFactory());
      }
      server.setSubsystemFactories(namedFactoryList);

      //
      for (AuthenticationPlugin authenticationPlugin : authenticationPlugins) {
        if (server.getPasswordAuthenticator() == null && authenticationPlugin.getCredentialType().equals(String.class)) {
          server.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String _username, String _password, ServerSession session) {
              if (genericAuthenticate(String.class, _username, _password)) {
                // We store username and password in session for later reuse
                session.setAttribute(USERNAME, _username);
                session.setAttribute(PASSWORD, _password);
                return true;
              } else {
                return false;
              }
            }
          });
        }

        if (server.getPublickeyAuthenticator() == null && authenticationPlugin.getCredentialType().equals(PublicKey.class)) {
          server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
              return genericAuthenticate(PublicKey.class, username, key);
            }
          });
        }
      }

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

  private <T> boolean genericAuthenticate(Class<T> type, String username, T credential) {
    for (AuthenticationPlugin authenticationPlugin : authenticationPlugins) {
      if (authenticationPlugin.getCredentialType().equals(type)) {
        try {
          log.log(Level.FINE, "Using authentication plugin " + authenticationPlugin + " to authenticate user " + username);
          @SuppressWarnings("unchecked")
          AuthenticationPlugin<T> authPlugin = (AuthenticationPlugin<T>) authenticationPlugin;
          if (authPlugin.authenticate(username, credential)) {
            return true;
          }
        } catch (Exception e) {
          log.log(Level.SEVERE, "Exception authenticating user " + username + " in authentication plugin: " + authenticationPlugin, e);
        }
      }
    }

    return false;
  }
}
