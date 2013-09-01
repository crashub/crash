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

package org.crsh.vfs;

import org.crsh.vfs.spi.FSDriver;
import org.crsh.vfs.spi.file.FileDriver;
import org.crsh.vfs.spi.url.URLDriver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class FS {

  /** . */
  final List<Mount<?>> mounts;

  public FS() {
    this.mounts = new ArrayList<Mount<?>>();
  }

  public File get(Path path) throws IOException {
    return new File(this, path);
  }

  public <H> FS mount(FSDriver<H> driver) {
    if (driver == null) {
      throw new NullPointerException();
    }
    mounts.add(Mount.wrap(driver));
    return this;
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
    URLDriver driver = new URLDriver();
    // Add the resources
    Enumeration<URL> en = cl.getResources(path.getValue().substring(1));
    while (en.hasMoreElements()) {
      URL url = en.nextElement();
      driver.merge(url);
    }

    return mount(driver);
  }

  public FS mount(Class<?> clazz) throws IOException, URISyntaxException {
    if (clazz == null) {
      throw new NullPointerException();
    }
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    URLDriver driver = new URLDriver();
    driver.merge(url);
    return mount(driver);
  }
}
