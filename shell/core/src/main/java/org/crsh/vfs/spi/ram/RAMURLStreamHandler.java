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

package org.crsh.vfs.spi.ram;

import org.crsh.vfs.Path;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class RAMURLStreamHandler extends URLStreamHandler {

  /** . */
  private final RAMDriver driver;

  public RAMURLStreamHandler(RAMDriver driver) {
    this.driver = driver;
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    Path path = Path.get(u.getFile());
    if (path.isDir()) {
      throw new IOException("Cannot open dir");
    }
    String file = driver.entries.get(path);
    if (file == null) {
      throw new IOException("Cannot open non existing dir " + path);
    }
    return new RAMURLConnection(u, file);
  }
}
