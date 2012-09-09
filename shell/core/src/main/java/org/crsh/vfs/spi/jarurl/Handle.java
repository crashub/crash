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

package org.crsh.vfs.spi.jarurl;

import org.crsh.vfs.Path;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;

public class Handle {

  /** . */
  private final JarURLDriver driver;

  /** . */
  final Path path;

  /** . */
  Map<String, Handle> children;

  /** . */
  JarEntry entry;

  Handle(JarURLDriver driver, String path) {
    this.driver = driver;
    this.path = Path.get("/" + path);
    this.children = new HashMap<String, Handle>();
  }

  public boolean isDir() {
     return entry == null || entry.isDirectory();
  }

  public URL toURL() throws IllegalArgumentException, IllegalStateException, MalformedURLException {
    if (isDir()) {
      throw new IllegalStateException("Cannot create dir URL");
    }
    String file = driver.jarURL.toString() + "!/" + entry.getName();
    return new URL("jar", "", file);
  }
}
