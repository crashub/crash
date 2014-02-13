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

import org.crsh.text.LineReader;
import org.crsh.text.LineRenderer;
import org.crsh.util.Pair;

class LabelLineRenderer extends LineRenderer {

  /** . */
  private final LabelElement element;

  LabelLineRenderer(LabelElement element) {
    this.element = element;
  }

  @Override
  public int getMinWidth() {
    return element.minWidth;
  }

  @Override
  public int getActualWidth() {
    return element.actualWidth;
  }

  @Override
  public int getActualHeight(int width) {
    return element.slicer.lines(width).length;
  }

  @Override
  public int getMinHeight(int width) {
    // For now we don't support cropping
    return getActualHeight(width);
  }

  @Override
  public LineReader reader(int width) {
    return reader(width, -1);
  }

  @Override
  public LineReader reader(final int width, int height) {
    if (width == 0) {
      return null;
    } else {
      Pair<Integer, Integer>[] lines = element.slicer.lines(width);
      if (height == -1) {
        height = lines.length;
      }
      if (lines.length > height) {
        return null;
      } else {
        return new LabelReader(element, lines, width, height);
      }
    }
  }
}
