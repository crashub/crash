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

import org.crsh.text.LineRenderer;
import org.crsh.text.Style;
import org.crsh.util.CharSlicer;
import org.crsh.util.Pair;

public class LabelElement extends Element {

  /** . */
  final String value;

  /** . */
  final int minWidth;

  /** . */
  final int actualWidth;

  /** . */
  final int actualHeight;

  /** . */
  final CharSlicer slicer;

  /**
   * Create a new label element
   *
   * @param value the label value
   * @param minWidth the label minimum width
   * @throws IllegalArgumentException if the minimum width is negative
   */
  public LabelElement(Object value, int minWidth) throws IllegalArgumentException {
    if (minWidth < 0) {
      throw new IllegalArgumentException("No negative min size allowed");
    }

    //
    String s = String.valueOf(value);

    // Determine size
    CharSlicer slicer = new CharSlicer(s);
    Pair<Integer, Integer> size = slicer.size();

    //
    this.value = s;
    this.minWidth = Math.min(size.getFirst(), minWidth);
    this.actualWidth = size.getFirst();
    this.actualHeight = size.getSecond();
    this.slicer = slicer;
  }

  public LabelElement(String value) {
    this((Object)value);
  }

  public LabelElement(String value, int minWidth) {
    this((Object)value, minWidth);
  }

  public LabelElement(Object value) {
    this(value, 1);
  }

  public String getValue() {
    return value;
  }

  public LineRenderer renderer() {
    return new LabelLineRenderer(this);
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
