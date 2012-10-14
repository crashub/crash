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

package org.crsh.text.ui;

import org.crsh.text.Renderer;
import org.crsh.text.Style;

public class LabelElement extends Element {

  /** . */
  final String value;

  /** . */
  final int minWidth;

  /** . */
  final int width;

  private static int width(String s, int index) {
    if (index < s.length()) {
      int pos = s.indexOf('\n', index + 1);
      if (pos == -1) {
        return s.length() - index;
      } else {
        return Math.max(pos - index, width(s, pos + 1));
      }
    } else {
      return 0;
    }
  }

  public LabelElement(String value, int minWidth) {
    if (minWidth < 0) {
      throw new IllegalArgumentException("No negative min size allowed");
    }

    // Determine width
    int width = width(value, 0);

    //
    this.value = value;
    this.minWidth = Math.min(width, minWidth);
    this.width = width;
  }

  public LabelElement(String value) {
    this(value, 1);
  }

  public LabelElement(Object value, int minWidth) {
    this(String.valueOf(value), minWidth);
  }

  public LabelElement(Object value) {
    this(String.valueOf(value));
  }

  public String getValue() {
    return value;
  }

  public Renderer renderer() {
    return new LabelRenderer(this);
  }

  @Override
  public String toString() {
    return "Label[" + value + "]";
  }

  @Override
  public LabelElement style(Style.Composite style) {
    return (LabelElement)super.style(style);
  }
}
