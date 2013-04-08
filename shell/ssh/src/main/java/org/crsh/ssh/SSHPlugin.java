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

import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ResourceKind;
import org.crsh.ssh.term.FilePublicKeyProvider;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.vfs.Resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.sshd.common.util.SecurityUtils;

public class SSHPlugin extends CRaSHPlugin<SSHPlugin> {

  /** . */
  private static final String[] TYPES = { KeyPairProvider.SSH_DSS, KeyPairProvider.SSH_RSA };

  /** The SSH port. */
  public static final PropertyDescriptor<Integer> SSH_PORT = PropertyDescriptor.create("ssh.port", 2000, "The SSH port");

  /** The SSH server key path. */
  public static final PropertyDescriptor<String> SSH_SERVER_KEYPATH = PropertyDescriptor.create("ssh.keypath", (String)null, "The path to the key file");

  /** The SSH authorized key path. */
  public static final PropertyDescriptor<String> SSH_AUTHORIZED_KEYPATH = PropertyDescriptor.create("ssh.authorized.keypath", (String)null, "The path to the authorized key file");

  /** . */
  private SSHLifeCycle lifeCycle;

  @Override
  public SSHPlugin getImplementation() {
    return this;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Arrays.<PropertyDescriptor<?>>asList(SSH_PORT, SSH_SERVER_KEYPATH, SSH_AUTHORIZED_KEYPATH, AuthenticationPlugin.AUTH);
  }

  @Override
  public void init() {

    SecurityUtils.setRegisterBouncyCastle(true);
    //
    Integer port = getContext().getProperty(SSH_PORT);
    if (port == null) {
      log.log(Level.INFO, "Could not boot SSHD due to missing due to missing port configuration");
      return;
    }

    //
    Resource serverKey = null;

    // Get embedded default key
    URL serverKeyURL = SSHPlugin.class.getResource("/crash/hostkey.pem");
    if (serverKeyURL != null) {
      try {
        log.log(Level.FINE, "Found embedded key url " + serverKeyURL);
        serverKey = new Resource(serverKeyURL);
      }
      catch (IOException e) {
        log.log(Level.FINE, "Could not load ssh key from url " + serverKeyURL, e);
      }
    }

    // Override from config if any
    Resource serverKeyRes = getContext().loadResource("hostkey.pem", ResourceKind.CONFIG);
    if (serverKeyRes != null) {
      serverKey = serverKeyRes;
      log.log(Level.FINE, "Found server ssh key url");
    }

    // If we have a key path, we convert is as an URL
    String serverKeyPath = getContext().getProperty(SSH_SERVER_KEYPATH);
    if (serverKeyPath != null) {
      log.log(Level.FINE, "Found server key path " + serverKeyPath);
      File f = new File(serverKeyPath);
      if (f.exists() && f.isFile()) {
        try {
          serverKeyURL = f.toURI().toURL();
        } catch (MalformedURLException e) {
          log.log(Level.FINE, "Ignoring invalid server key " + serverKeyPath, e);
        }
      } else {
        log.log(Level.FINE, "Ignoring invalid server key path " + serverKeyPath);
      }
    }

    //
    if (serverKeyURL == null) {
      log.log(Level.INFO, "Could not boot SSHD due to missing server key");
      return;
    }

    //
    Set<PublicKey> authorizedKeys = Collections.emptySet();
    String authorizedKeyPath = getContext().getProperty(SSH_AUTHORIZED_KEYPATH);
    if (authorizedKeyPath != null) {
      File f = new File(authorizedKeyPath);
      if (f.exists() && f.isFile()) {
        log.log(Level.FINE, "Found authorized key path " + authorizedKeyPath);
        authorizedKeys = new LinkedHashSet<PublicKey>();
        KeyPairProvider provider = new FilePublicKeyProvider(new String[]{authorizedKeyPath});
        for (String type : TYPES) {
          KeyPair pair = provider.loadKey(type);
          if (pair != null) {
            PublicKey key = pair.getPublic();
            if (key != null) {
              authorizedKeys.add(key);
            }
          }
        }
      } else {
        log.log(Level.FINE, "Ignoring invalid authorized key path " + authorizedKeyPath);
      }
    }

    // Get the authentication
    AuthenticationPlugin authPlugin = AuthenticationPlugin.NULL;
    String authentication = getContext().getProperty(AuthenticationPlugin.AUTH);
    if (authentication != null) {
      for (AuthenticationPlugin authenticationPlugin : getContext().getPlugins(AuthenticationPlugin.class)) {
        if (authentication.equals(authenticationPlugin.getName())) {
          authPlugin = authenticationPlugin;
          break;
        }
      }
    }

    //
    log.log(Level.INFO, "Booting SSHD");
    SSHLifeCycle lifeCycle = new SSHLifeCycle(getContext(), authPlugin, authorizedKeys);
    lifeCycle.setPort(port);
    lifeCycle.setKey(serverKey);
    lifeCycle.init();

    //
    this.lifeCycle = lifeCycle;
  }

  @Override
  public void destroy() {
    if (lifeCycle != null) {
      log.log(Level.INFO, "Shutting down SSHD");
      lifeCycle.destroy();
      lifeCycle = null;
    }
  }
}
