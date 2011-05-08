/*
 * Copyright (C) 2010 eXo Platform SAS.
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
import org.crsh.plugin.Service;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.vfs.Resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHPlugin extends CRaSHPlugin<SSHPlugin> implements Service {

  /** . */
  private SSHLifeCycle lifeCycle;

  @Override
  public SSHPlugin getImplementation() {
    return this;
  }

  @Override
  public void init() {

    //
    Integer port = getContext().getProperty(PropertyDescriptor.SSH_PORT);
    if (port == null) {
      log.info("Could not boot SSHD due to missing due to missing port configuration");
      return;
    }

    //
    Resource res = getContext().loadResource("hostkey.pem", ResourceKind.KEY);
    URL keyURL = null;
    if (res != null) {
      keyURL = res.getURL();
      log.debug("Found key url " + keyURL);
    }

    // If we have a key path, we convert is as an URL
    String keyPath = getContext().getProperty(PropertyDescriptor.SSH_KEYPATH);
    if (keyPath != null) {
      log.debug("Found key path " + keyPath);
      File f = new File(keyPath);
      if (f.exists() && f.isFile()) {
        try {
          keyURL = f.toURI().toURL();
        } catch (MalformedURLException e) {
          log.debug("Ignoring invalid key " + keyPath, e);
        }
      } else {
        log.debug("Ignoring invalid key path " + keyPath);
      }
    }

    //
    if (keyURL == null) {
      log.info("Could not boot SSHD due to missing key");
      return;
    }

    //
    log.info("Booting SSHD");
    SSHLifeCycle lifeCycle = new SSHLifeCycle(getContext());
    lifeCycle.setPort(port);
    lifeCycle.setKeyURL(keyURL);
    lifeCycle.init();

    //
    this.lifeCycle = lifeCycle;
  }

  @Override
  public void destroy() {
    if (lifeCycle != null) {
      log.info("Shutting down SSHD");
      lifeCycle.destroy();
      lifeCycle = null;
    }
  }
}
