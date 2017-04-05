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

import org.apache.sshd.common.keyprovider.KeyPairProvider;
//import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ResourceKind;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.ssh.term.URLKeyPairProvider;
import org.crsh.util.Utils;
import org.crsh.vfs.Resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.sshd.common.util.SecurityUtils;

public class SSHPlugin extends CRaSHPlugin<SSHPlugin> {

  /** The SSH server idle timeout value. */
  private static final int SSH_SERVER_IDLE_DEFAULT_TIMEOUT = 10 * 60 * 1000;

  /** The SSH server authentication timeout value. */
  private static final int SSH_SERVER_AUTH_DEFAULT_TIMEOUT = 10 * 60 * 1000;

  /** The SSH port. */
  public static final PropertyDescriptor<Integer> SSH_PORT = PropertyDescriptor.create("ssh.port", 2000, "The SSH port");

  /** The SSH server key path. */
  public static final PropertyDescriptor<String> SSH_SERVER_KEYPATH = PropertyDescriptor.create("ssh.keypath", (String)null, "The path to the key file");

  /** SSH host key auto generate */
  public static final PropertyDescriptor<String> SSH_SERVER_KEYGEN = PropertyDescriptor.create("ssh.keygen", "false", "Whether to automatically generate a host key");

  /** The SSH server idle timeout property. */
  public static final PropertyDescriptor<Integer> SSH_SERVER_IDLE_TIMEOUT = PropertyDescriptor.create("ssh.idle_timeout", SSH_SERVER_IDLE_DEFAULT_TIMEOUT, "The idle-timeout for ssh sessions in milliseconds");

  /** The SSH server authentication timeout property. */
  public static final PropertyDescriptor<Integer> SSH_SERVER_AUTH_TIMEOUT = PropertyDescriptor.create("ssh.auth_timeout", SSH_SERVER_AUTH_DEFAULT_TIMEOUT, "The authentication timeout for ssh sessions in milliseconds");

  /** The SSH charset. */
  public static final PropertyDescriptor<Charset> SSH_ENCODING = new PropertyDescriptor<Charset>(Charset.class, "ssh.default_encoding", Utils.UTF_8, "The ssh stream default encoding when no one could be determined") {
    @Override
    protected Charset doParse(String s) throws Exception {
      return Charset.forName(s);
    }
  };

  /** . */
  private SSHLifeCycle lifeCycle;

  @Override
  public SSHPlugin getImplementation() {
    return this;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Arrays.<PropertyDescriptor<?>>asList(SSH_PORT, SSH_SERVER_KEYPATH, SSH_SERVER_KEYGEN, SSH_SERVER_AUTH_TIMEOUT,
        SSH_SERVER_IDLE_TIMEOUT, SSH_ENCODING, AuthenticationPlugin.AUTH);
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
    Integer idleTimeout = getContext().getProperty(SSH_SERVER_IDLE_TIMEOUT);
    if (idleTimeout == null) {
      idleTimeout = SSH_SERVER_IDLE_DEFAULT_TIMEOUT;
    }
    Integer authTimeout = getContext().getProperty(SSH_SERVER_AUTH_TIMEOUT);
    if (authTimeout == null) {
      authTimeout = SSH_SERVER_AUTH_DEFAULT_TIMEOUT;
    }

    //
    Resource serverKey = null;
    KeyPairProvider keyPairProvider = null;

    // Get embedded default key
    URL serverKeyURL = SSHPlugin.class.getResource("/crash/hostkey.pem");
    if (serverKeyURL != null) {
      try {
        log.log(Level.FINE, "Found embedded key url " + serverKeyURL);
        serverKey = new Resource("hostkey.pem", serverKeyURL);
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
      String keyGen = getContext().getProperty(SSH_SERVER_KEYGEN);
      if (keyGen != null && keyGen.equals("true")) {
        // keyPairProvider = new PEMGeneratorHostKeyProvider(serverKeyPath, "RSA");
        keyPairProvider = new SimpleGeneratorHostKeyProvider(new File(serverKeyPath));
      } else if (f.exists() && f.isFile()) {
        try {
          serverKeyURL = f.toURI().toURL();
          serverKey = new Resource("hostkey.pem", serverKeyURL);
        } catch (MalformedURLException e) {
          log.log(Level.FINE, "Ignoring invalid server key " + serverKeyPath, e);
        } catch (IOException e) {
          log.log(Level.FINE, "Could not load SSH key from " + serverKeyPath, e);
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
    if (keyPairProvider == null) {
      keyPairProvider = new URLKeyPairProvider(serverKey);
    }

    // Get the authentication
    ArrayList<AuthenticationPlugin> authPlugins = new ArrayList<AuthenticationPlugin>(0);
    List authentication = getContext().getProperty(AuthenticationPlugin.AUTH);
    if (authentication != null) {
      for (AuthenticationPlugin authenticationPlugin : getContext().getPlugins(AuthenticationPlugin.class)) {
        if (authentication.contains(authenticationPlugin.getName())) {
          authPlugins.add(authenticationPlugin);
        }
      }
    }

    //
    Charset encoding = getContext().getProperty(SSH_ENCODING);
    if (encoding == null) {
      encoding = Utils.UTF_8;
    }

    //
    log.log(Level.INFO, "Booting SSHD");
    SSHLifeCycle lifeCycle = new SSHLifeCycle(
        getContext(),
        encoding,
        port,
        idleTimeout,
        authTimeout,
        keyPairProvider,
        authPlugins);
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
