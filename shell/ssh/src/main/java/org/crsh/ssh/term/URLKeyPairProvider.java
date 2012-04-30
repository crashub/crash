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

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.util.SecurityUtils;
import org.bouncycastle.openssl.PEMReader;
import org.crsh.vfs.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class URLKeyPairProvider extends AbstractKeyPairProvider {

  /** . */
  private static final Logger LOG = LoggerFactory.getLogger(URLKeyPairProvider.class);

  /** . */
  private final Resource key;

  public URLKeyPairProvider(Resource key) {
    this.key = key;
  }

  @Override
  protected KeyPair[] loadKeys() {
    if (!SecurityUtils.isBouncyCastleRegistered()) {
      throw new IllegalStateException("BouncyCastle must be registered as a JCE provider");
    }
    List<KeyPair> keys = new ArrayList<KeyPair>();
    if (key != null) {
      try {
        PEMReader r = new PEMReader(new InputStreamReader(new ByteArrayInputStream(key.getContent())));
        try {
          Object o = r.readObject();
          if (o instanceof KeyPair) {
            keys.add((KeyPair) o);
          }
        } finally {
          r.close();
        }
      } catch (Exception e) {
        LOG.info("Unable to read key {}: {}", key, e);
      }
    }
    return keys.toArray(new KeyPair[keys.size()]);
  }
}
