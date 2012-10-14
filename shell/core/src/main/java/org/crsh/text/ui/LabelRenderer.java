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
import org.crsh.text.Renderer;
import org.crsh.text.Style;

class LabelRenderer extends Renderer {

  /** . */
  private final LabelElement label;

  LabelRenderer(LabelElement label) {
    this.label = label;
  }

  @Override
  public int getMinWidth() {
    return label.minWidth;
  }

  @Override
  public int getActualWidth() {
    return label.width;
  }

  @Override
  public LineReader renderer(final int width) {

    if (width == 0) {
      return new LineReader() {
        boolean done = false;
        public boolean hasLine() {
          return !done;
        }
        public void renderLine(RenderAppendable to) throws IllegalStateException {
          if (done) {
            throw new IllegalStateException();
          } else {
            done = true;
          }
        }
      };
    } else {
      return new LineReader() {

        /** . */
        boolean done = false;

        /** . */
        int index = 0;

        public boolean hasLine() {
          return !done;
        }

        public void renderLine(RenderAppendable to) {
          if (done) {
            throw new IllegalStateException();
          } else {
            Style.Composite style = label.getStyle();
            if (style != null) {
              to.enterStyle(style);
            }
            int pos = label.value.indexOf('\n', index);
            int next;
            if (pos == -1) {
              pos = Math.min(index + width, label.value.length());
              next = pos;
            } else {
              if (pos <= index + width) {
                next = pos + 1;
              } else {
                next = pos = index + width;
              }
            }
            to.append(label.value, index, pos);
            int missing = pos - index;
            while (missing < width) {
              to.append(' ');
              missing++;
            }
            index = next;
            done = index >= label.value.length();
            if (style != null) {
              to.leaveStyle();
            }
          }
        }
      };
    }
  }
}
