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

package org.crsh.jcr.command;

public class Path {

  /** . */
  public static final Path ROOT = new Path("/");

  /** . */
  private final String value;

  public Path(String string) throws NullPointerException {
    if (string == null) {
      throw new NullPointerException("No null value accepted");
    }

    //
    this.value = string;
  }

  public boolean isAbsolute() {
    return value.startsWith("/");
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else {
      if (obj instanceof Path) {
        Path that = (Path)obj;
        return value.equals(that.value);
      } else {
        return false;
      }
    }
  }

  @Override
  public String toString() {
    return "Path[" + value + "]";
  }
}
