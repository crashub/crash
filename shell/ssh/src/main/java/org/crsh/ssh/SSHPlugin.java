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

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ResourceKind;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.vfs.Resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;

import org.apache.sshd.common.util.SecurityUtils;

public class SSHPlugin extends CRaSHPlugin<SSHPlugin> {

  /** The SSH port. */
  public static final PropertyDescriptor<Integer> SSH_PORT = PropertyDescriptor.create("ssh.port", 2000, "The SSH port");

  /** The SSH key path. */
  public static final PropertyDescriptor<String> SSH_KEYPATH = PropertyDescriptor.create("ssh.keypath", (String)null, "The path to the key file");

  /** The authentication plugin to use. */
  public static final PropertyDescriptor<String> AUTH = PropertyDescriptor.create("auth", (String)null, "The authentication plugin");

  /** . */
  private SSHLifeCycle lifeCycle;

  @Override
  public SSHPlugin getImplementation() {
    return this;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Arrays.<PropertyDescriptor<?>>asList(SSH_PORT, SSH_KEYPATH, AUTH);
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
    Resource key = null;

    // Get embedded default key
    URL keyURL = SSHPlugin.class.getResource("hostkey.pem");
    if (keyURL != null) {
      try {
        log.log(Level.FINE, "Found embedded key url " + keyURL);
        key = new Resource(keyURL);
      }
      catch (IOException e) {
        log.log(Level.FINE, "Could not load ssh key from url " + keyURL, e);
      }
    }

    // Override from config if any
    Resource res = getContext().loadResource("hostkey.pem", ResourceKind.CONFIG);
    if (res != null) {
      key = res;
      log.log(Level.FINE, "Found ssh key url");
    }

    // If we have a key path, we convert is as an URL
    String keyPath = getContext().getProperty(SSH_KEYPATH);
    if (keyPath != null) {
      log.log(Level.FINE, "Found key path " + keyPath);
      File f = new File(keyPath);
      if (f.exists() && f.isFile()) {
        try {
          keyURL = f.toURI().toURL();
        } catch (MalformedURLException e) {
          log.log(Level.FINE, "Ignoring invalid key " + keyPath, e);
        }
      } else {
        log.log(Level.FINE, "Ignoring invalid key path " + keyPath);
      }
    }

    //
    if (keyURL == null) {
      log.log(Level.INFO, "Could not boot SSHD due to missing key");
      return;
    }

    // Get the authentication
    String authentication = getContext().getProperty(AUTH);

    //
    log.log(Level.INFO, "Booting SSHD");
    SSHLifeCycle lifeCycle = new SSHLifeCycle(getContext());
    lifeCycle.setPort(port);
    lifeCycle.setKey(key);
    lifeCycle.setAuthentication(authentication);
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
