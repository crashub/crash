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
package org.crsh.ssh.util;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemReader;
import java.io.Reader;

/** @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a> */
public class KeyPairUtils {
    public static Object readKey(Reader reader) throws Exception {
        try {
            PEMParser pemParser = new PEMParser(reader);
            try {
                return pemParser.readObject();
            } finally {
                pemParser.close();
            }
        } catch (NoClassDefFoundError e) {
            //. We use reflection here to keep compatible with old library of bouncycastle
            Class<?> pemReaderClass = Class.forName("org.bouncycastle.openssl.PEMReader");
            PemReader r = (PemReader)pemReaderClass.getConstructor(Reader.class).newInstance(reader);
            try {
                return pemReaderClass.getMethod("readObject").invoke(r);
            } finally {
                r.close();
            }
        }
    }
}
