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

package org.crsh.vfs;

import org.crsh.vfs.spi.FSDriver;
import org.crsh.vfs.spi.file.FileDriver;
import org.crsh.vfs.spi.jarurl.JarURLDriver;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FS {

  /** . */
  final List<Mount<?>> mounts;

  public FS(FSDriver<?> driver) {
    this.mounts = Arrays.<Mount<?>>asList(Mount.wrap(driver));
  }

  public File get(Path path) throws IOException {
    return new File(this, path);
  }

  public static FSDriver getDriver(URLClassLoader cl) throws IOException, URISyntaxException {
    if (cl == null) {
      throw new NullPointerException();
    }
    return null;
  }

  public static FSDriver getDriver(Class<?> clazz) throws IOException, URISyntaxException {
    if (clazz == null) {
      throw new NullPointerException();
    }
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    String protocol = url.getProtocol();
    if (protocol.equals("file")) {
      return new FileDriver(new java.io.File(url.toURI()));
    } else if (protocol.equals("jar")) {
      JarURLConnection conn = (JarURLConnection)url.openConnection();
      return new JarURLDriver(conn);
    } else {
      throw new IllegalArgumentException("Protocol " + protocol + " not supported");
    }
  }
}
