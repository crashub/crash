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

package org.crsh.cmdline.matcher;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
final class StringCursor {

  /** . */
  private int index;

  /** . */
  private final String s;

  StringCursor(String s) {
    this.s = s;
    this.index = 0;
  }

  public int getIndex() {
    return index;
  }

  String getValue() {
    return s.substring(index);
  }

  void seek(int to) {
    skip(to - index);
  }

  void skip(int delta) {
    if (delta < 0) {
      throw new AssertionError();
    }
    index += delta;
  }

  boolean isEmpty() {
    return index == s.length();
  }

  int length() {
    return s.length() - index;
  }
}
