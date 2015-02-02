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
package org.crsh.auth;

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.util.SecurityUtils;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.crsh.ssh.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * A modified version of {@link org.apache.sshd.common.keyprovider.FileKeyPairProvider} that only load public keys
 * either as {@link KeyPair} or as {@link PublicKey}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class FilePublicKeyProvider extends AbstractKeyPairProvider {

  /** . */
  private static final Logger LOG = LoggerFactory.getLogger(FilePublicKeyProvider.class);

  /** . */
  private String[] files;

  FilePublicKeyProvider(String[] files) {
    this.files = files;
  }

  public Iterable<KeyPair> loadKeys() {
    if (!SecurityUtils.isBouncyCastleRegistered()) {
      throw new IllegalStateException("BouncyCastle must be registered as a JCE provider");
    }
    List<KeyPair> keys = new ArrayList<KeyPair>();
    for (String file : files) {
      try {
          Object o = KeyPairUtils.readKey(new InputStreamReader(new FileInputStream(file)));
          if (o instanceof KeyPair) {
            keys.add(new KeyPair(((KeyPair)o).getPublic(), null));
          } else if (o instanceof PublicKey) {
            keys.add(new KeyPair((PublicKey)o, null));
          } else if (o instanceof PEMKeyPair) {
            PEMKeyPair keyPair = (PEMKeyPair)o;
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            keys.add(new KeyPair(converter.getPublicKey(keyPair.getPublicKeyInfo()), null));
          }
      }
      catch (Exception e) {
        LOG.info("Unable to read key {}: {}", file, e);
      }
    }
    return keys;
  }
}
