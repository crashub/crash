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
import org.apache.sshd.server.session.ServerSession;
import org.crsh.plugin.PluginContext;
import org.crsh.ssh.AuthenticationPlugin;
import org.crsh.ssh.term.scp.SCPCommandFactory;
import org.crsh.term.TermLifeCycle;
import org.crsh.term.spi.TermIOHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHLifeCycle extends TermLifeCycle {

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
  private URL keyURL;

  public SSHLifeCycle(PluginContext context) {
    super(context);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public URL getKeyURL() {
    return keyURL;
  }

  public void setKeyURL(URL keyURL) {
    this.keyURL = keyURL;
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
      server.setKeyPairProvider(new URLKeyPairProvider(keyURL));

      AuthenticationPlugin plugin = null;
      Iterator<AuthenticationPlugin> plugins = getContext().getPlugins(AuthenticationPlugin.class).iterator();
      if (plugins.hasNext()) {
        plugin = plugins.next();
        if (plugins.hasNext()) {
          throw new RuntimeException("More then one authentication plugin detected.");
        }
      }

      final AuthenticationPlugin authPlugin = plugin;
      //
      server.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String _username, String _password, ServerSession session) {
          boolean auth = true;
          if (authPlugin != null)
          {
            try {
              log.debug("Using authentication plugin " + authPlugin + " to authenticate user " + _username);
              auth = authPlugin.authenticate(_username, _password);
            } catch (Exception e) {
              log.error("Exception authenticating user " + _username + " in authentication plugin: " + authPlugin, e);
              return false;
            }
          }
          //TODO: I don't think we should store the password as an attribute in the server session for security reasons.
//          if (auth) {
//            session.setAttribute(USERNAME, _username);
//            session.setAttribute(PASSWORD, _password);
//          }

          return auth;
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
