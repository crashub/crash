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
import org.crsh.plugin.Property;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ResourceKind;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.vfs.Resource;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHPlugin extends CRaSHPlugin {

  /** . */
  private SSHLifeCycle lifeCycle;

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
    }

    //
    String keyPath = getContext().getProperty(PropertyDescriptor.SSH_KEYPATH);
    if (keyPath == null) {
      Resource r = getContext().loadResource("hostkey.pem", ResourceKind.KEY);
      if (r != null) {
        // Use the default one
        log.debug("No key path found in web.xml will try to use the default one");
        URL url = r.getURL();
        if (url != null) {
          if ("file".equals(url.getProtocol())) {
            try {
              File file = new File(url.toURI());
              keyPath = file.getAbsolutePath();
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    }

    //
    if (keyPath == null) {
      if (keyURL == null) {
        log.info("Could not boot SSHD due to missing key");
        return;
      }
    }

    //
    log.info("Booting SSHD");
    SSHLifeCycle lifeCycle = new SSHLifeCycle(getContext());
    lifeCycle.setKeyPath(keyPath);
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
