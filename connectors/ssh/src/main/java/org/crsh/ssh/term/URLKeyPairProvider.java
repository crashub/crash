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

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.crsh.ssh.util.KeyPairUtils;
import org.crsh.vfs.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class URLKeyPairProvider extends AbstractKeyPairProvider {

  /** . */
  private static final Logger log = Logger.getLogger(URLKeyPairProvider.class.getName());

  /** . */
  private final Resource key;

  public URLKeyPairProvider(Resource key) {
    this.key = key;
  }

  @Override
  public Iterable<java.security.KeyPair> loadKeys() {
    if (!SecurityUtils.isBouncyCastleRegistered()) {
      throw new IllegalStateException("BouncyCastle must be registered as a JCE provider");
    }
    List<KeyPair> keys = new ArrayList<KeyPair>();
    if (key != null) {
      try {
          Object o = KeyPairUtils.readKey(new InputStreamReader(new ByteArrayInputStream(key.getContent())));
          if (o instanceof KeyPair) {
            keys.add((KeyPair) o);
          } else if(o instanceof PEMKeyPair) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            keys.add(converter.getKeyPair((PEMKeyPair)o));
          }
      } catch (Exception e) {
        log.log(Level.INFO, "Unable to read key " + key + ": " + key, e);
      }
    }
    return keys;
  }
}
