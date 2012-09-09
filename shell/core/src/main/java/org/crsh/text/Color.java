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

package org.crsh.text;

import java.io.Serializable;

public enum Color implements Serializable {

  black(0),
  red(1),
  green(2),
  yellow(3),
  blue(4),
  magenta(5),
  cyan(6),
  white(7);

  /** . */
  public final int code;

  /** . */
  public final Style style;

  private Color(int code) {
    this.code = code;
    this.style = Style.style(this);
  }

  public int code(int base) {
    return base + code;
  }
}
