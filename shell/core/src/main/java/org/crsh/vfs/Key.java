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

class Key {

  /** . */
  final String name;

  /** . */
  final boolean dir;

  Key(String name, boolean dir) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.dir = dir;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Key) {
      Key that = (Key)obj;
      return name.equals(that.name) && dir == that.dir;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ (dir ? 0xFFFFFFFF : 0);
  }
}
