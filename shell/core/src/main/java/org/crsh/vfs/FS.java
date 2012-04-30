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
import org.crsh.vfs.spi.mount.MountDriver;
import org.crsh.vfs.spi.servlet.ServletContextDriver;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FS {

  /** . */
  final List<Mount<?>> mounts;

  public FS() {
    this.mounts = new ArrayList<Mount<?>>();
  }

  public File get(Path path) throws IOException {
    return new File(this, path);
  }

  public <H> FS mount(FSDriver<H> driver, Path path) {
    if (driver == null) {
      throw new NullPointerException();
    }
    if (path.equals(Path.get("/"))) {
      mounts.add(Mount.wrap(driver));
    } else {
      mounts.add(Mount.wrap(new MountDriver<H>(path, driver)));
    }
    return this;
  }

  public <H> FS mount(FSDriver<H> driver, String path) {
    return mount(driver, Path.get(path));
  }

  public <H> FS mount(FSDriver<H> driver) {
    return mount(driver, "/");
  }

  public FS mount(java.io.File root) {
    return mount(new FileDriver(root));
  }

  public FS mount(ClassLoader cl, Path path) throws IOException, URISyntaxException {
    if (cl == null) {
      throw new NullPointerException();
    }
    if (path == null) {
      throw new NullPointerException();
    }
    if (!path.isDir()) {
      throw new IllegalArgumentException("Path " + path + " must be a dir");
    }
    Enumeration<URL> en = cl.getResources(path.getValue().substring(1));
    while (en.hasMoreElements()) {
      URL url = en.nextElement();
      String protocol = url.getProtocol();
      if ("file".equals(protocol)) {
        java.io.File root = new java.io.File(url.toURI());
        mount(root);
      } else if ("jar".equals(protocol)) {
        JarURLConnection conn = (JarURLConnection)url.openConnection();
        JarURLDriver jarDriver = new JarURLDriver(cl, conn);
        mount(jarDriver, path);
      }
    }
    return this;
  }

  public FS mount(Class<?> clazz) throws IOException, URISyntaxException {
    if (clazz == null) {
      throw new NullPointerException();
    }
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    String protocol = url.getProtocol();
    FSDriver<?> driver;
    if (protocol.equals("file")) {
      driver = new FileDriver(new java.io.File(url.toURI()));
    } else if (protocol.equals("jar")) {
      JarURLConnection conn = (JarURLConnection)url.openConnection();
      driver = new JarURLDriver(clazz.getClassLoader(), conn);
    } else {
      throw new IllegalArgumentException("Protocol " + protocol + " not supported");
    }
    return mount(driver);
  }
}
