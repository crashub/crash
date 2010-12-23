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

package org.crsh.cmdline.analyzer;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
class StringCursor {

  /** . */
  private StringBuilder done;

  /** The rest. */
  private String rest;

  StringCursor(String rest) {
    this.rest = rest;
    this.done = new StringBuilder();
  }

  public int getCount() {
    return done.length();
  }

  String getRest() {
    return rest;
  }

  void seek(int to) {
    skip(to - done.length());
  }

  void skip(int delta) {
    if (delta < 0) {
      throw new AssertionError();
    }
    done.append(rest.substring(0, delta));
    rest = rest.substring(delta);
  }
}
