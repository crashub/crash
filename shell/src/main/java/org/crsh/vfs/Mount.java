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

import java.io.IOException;

class Mount<H> {

  static <H> Mount<H> wrap(FSDriver<H> driver) {
    return new Mount<H>(driver);
  }

  /** . */
  final FSDriver<H> driver;

  /**
   * Create a new mount
   *
   * @param driver the driver
   * @throws NullPointerException if the driver is null
   */
  Mount(FSDriver<H> driver) throws NullPointerException {
    if (driver == null) {
      throw new NullPointerException("No null driver accepted");
    }
    this.driver = driver;
  }

  Handle<H> getHandle(Path path) throws IOException {
    H current = driver.root();
    for (String name : path) {
      H next = null;
      for (H child : driver.children(current)) {
        String childName = driver.name(child);
        if (childName.equals(name)) {
          next = child;
          break;
        }
      }
      if (next == null) {
        return null;
      } else {
        current = next;
      }
    }
    if (path.isDir() == driver.isDir(current)) {
      return new Handle<H>(driver, current);
    } else {
      return null;
    }
  }
}
