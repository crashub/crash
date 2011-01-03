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

package org.crsh.ssh.term;

import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.crsh.util.Safe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyPair;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSHPEMGeneratorHostKeyProvider extends PEMGeneratorHostKeyProvider {

  /** . */
  private static final Logger LOG = LoggerFactory.getLogger(CRaSHPEMGeneratorHostKeyProvider.class);

  /** . */
  private URL url;

  /** . */
  private KeyPair[] keyPair;

  public CRaSHPEMGeneratorHostKeyProvider(String path, URL url) {
    super(path);

    //
    this.url = url;
  }

  public URL getURL() {
    return url;
  }

  public void setURL(URL url) {
    this.url = url;
  }

  public synchronized KeyPair[] loadKeys() {
    if (keyPair == null) {
      KeyPair[] superKeyPair = super.loadKeys();
      if (superKeyPair.length > 0) {
        keyPair = superKeyPair;
      }
    }
    if (keyPair == null) {
      if (url != null) {
        KeyPair generated = readKeyPair(url);
        if (generated != null) {
          keyPair = new KeyPair[]{generated};
        }
      }
    }
    if (keyPair == null) {
      keyPair = new KeyPair[0];
    }
    return keyPair.clone();
  }

  private KeyPair readKeyPair(URL url) {
    InputStream is = null;
    try {
      is = url.openStream();
      return doReadKeyPair(is);
    }
    catch (Exception e) {
      LOG.info("Unable to read key {}: {}", url, e);
    }
    finally {
      Safe.close(is);
    }
    return null;
  }
}
