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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An abstraction for a virtual file system.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class VFS<H> {

  public static <H> VFS<H> wrap(FSDriver<H> driver) {
    return new VFS<H>(driver);
  }

  /** . */
  final FSDriver<H> driver;

  /** . */
  private final ConcurrentMap<H, File<H>> map;

  protected VFS(FSDriver<H> driver) throws NullPointerException {
    if (driver == null) {
      throw new NullPointerException();
    }
    this.driver = driver;
    this.map = new ConcurrentHashMap<H, File<H>>();
  }

  public File<H> getRoot() throws IOException {
    return get(driver.root());
  }

  File<H> get(H handle) {
    File<H> file = map.get(handle);
    if (file == null) {
      file = new File<H>(this, handle);
      File<H> phantom = map.putIfAbsent(handle, file);
      if (phantom != null) {
        file = phantom;
      }
    }
    return file;
  }
}
