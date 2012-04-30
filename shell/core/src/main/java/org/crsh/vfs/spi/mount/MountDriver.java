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

package org.crsh.vfs.spi.mount;

import org.crsh.vfs.Path;
import org.crsh.vfs.spi.AbstractFSDriver;
import org.crsh.vfs.spi.FSDriver;

import java.io.IOException;
import java.io.InputStream;

/**
 * The mount driver mounts path of a driver.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MountDriver<H> extends AbstractFSDriver<H> {

  /** . */
  private final Path path;

  /** . */
  private final FSDriver<H> driver;

  public MountDriver(Path path, FSDriver<H> driver) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (driver == null) {
      throw new NullPointerException();
    }
    if (!path.isDir()) {
      throw new IllegalArgumentException("Mount path must be a dir");
    }

    //
    this.path = path;
    this.driver = driver;
  }

  public H root() throws IOException {
    H root = driver.root();
    for (String name : path) {
      root = driver.child(root, name);
      if (root == null) {
        break;
      }
    }
    return root;
  }

  public String name(H handle) throws IOException {
    return driver.name(handle);
  }

  public boolean isDir(H handle) throws IOException {
    return driver.isDir(handle);
  }

  public Iterable<H> children(H handle) throws IOException {
    return driver.children(handle);
  }

  public long getLastModified(H handle) throws IOException {
    return driver.getLastModified(handle);
  }

  public InputStream open(H handle) throws IOException {
    return driver.open(handle);
  }
}
