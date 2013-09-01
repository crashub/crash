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
import org.crsh.text.RenderAppendable;
import org.crsh.text.Style;
import org.crsh.util.BlankSequence;
import org.crsh.util.Pair;

class LabelReader implements LineReader {

  /** . */
  private final LabelElement element;

  /** . */
  private final Pair<Integer, Integer>[] lines;

  /** . */
  private final int width;

  /** . */
  private final int height;

  LabelReader(LabelElement element, Pair<Integer, Integer>[] lines, int width, int height) {
    this.element = element;
    this.lines = lines;
    this.height = height;
    this.width = width;
  }

  /** . */
  private int index = 0;

  public boolean hasLine() {
    return index < height;
  }

  public void renderLine(RenderAppendable to) {
    if (index >= height) {
      throw new IllegalStateException();
    } else {
      Style.Composite style = element.getStyle();
      if (style != null) {
        to.enterStyle(style);
      }
      if (index < lines.length) {
        Pair<Integer, Integer> a = lines[index];
        to.append(element.value, a.getFirst(), a.getSecond());
        int missing = width - (a.getSecond() - a.getFirst());
        if (missing > 0) {
          to.append(BlankSequence.create(missing));
        }
      } else {
        for (int i = 0;i < width;i++) {
          to.append(' ');
        }
      }
      index++;
      if (style != null) {
        to.leaveStyle();
      }
    }
  }
}
