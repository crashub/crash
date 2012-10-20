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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class TableRenderer extends Renderer {

  /** . */
  private final List<RowRenderer> rows;

  /** . */
  private final ColumnLayout layout;

  /** . */
  private final Border border;

  /** . */
  private final Style.Composite style;

  /** . */
  private final Integer height;

  TableRenderer(TableElement table) {

    //
    List<RowRenderer> rows = new ArrayList<RowRenderer>(table.getRows().size());
    for (RowElement row : table.getRows()) {
      rows.add(row.renderer());
    }

    //
    this.rows = rows;
    this.layout = table.getLayout();
    this.border = table.getBorder();
    this.style = table.getStyle();
    this.height = table.getHeight();
  }

  private int getMaxColSize() {
    int n = 0;
    for (RowRenderer row : rows) {
      n = Math.max(n, row.getSize());
    }
    return n;
  }

  @Override
  public int getMinWidth() {
    int width = 0;
    for (RowRenderer row : rows) {
      width = Math.max(width, row.getMinWidth());
    }
    return width;
  }

  @Override
  public int getActualWidth() {
    int width = 0;
    for (RowRenderer row : rows) {
      width = Math.max(width, row.getActualWidth());
    }
    return width;
  }

  @Override
  public LineReader renderer(final int width) {

    int len = getMaxColSize();
    int[] eltWidths = new int[len];
    int[] eltMinWidths = new int[len];

    // Compute each column as is
    for (RowRenderer row : rows) {
      for (int i = 0;i < row.getCols().size();i++) {
        Renderer renderable = row.getCols().get(i);
        eltWidths[i] = Math.max(eltWidths[i], renderable.getActualWidth());
        eltMinWidths[i] = Math.max(eltMinWidths[i], renderable.getMinWidth());
      }
    }

    // Note that we may have a different widths != eltWidths according to the layout algorithm
    final int[] widths = layout.compute(border, width, eltWidths, eltMinWidths);

    //
    if (widths == null) {
      return new LineReader() {
        public int getWidth() {
          return width;
        }
        public boolean hasLine() {
          return false;
        }
        public void renderLine(RenderAppendable to) throws IllegalStateException {
          throw new IllegalStateException();
        }
      };
    } else {
      final LinkedList<Object> renderers = new LinkedList<Object>();

      // Add all rows
      boolean prev = false;
      for (int i = 0;i < rows.size();i++) {
        RowRenderer row = rows.get(i);
        if (border != null && (row.isHeader() || i == 0) && !prev) {
          renderers.add(border);
        }

        //
        int[] bilto = layout.compute(
            border,
            width,
            Arrays.copyOf(eltWidths, row.getCols().size()),
            Arrays.copyOf(eltMinWidths, row.getCols().size()));

        //
        renderers.add(row.renderer(bilto, width, border));

        //
        if (border != null && (row.isHeader() || i == rows.size() - 1)) {
          renderers.add(border);
          prev = true;
        } else {
          prev = false;
        }
      }

      //
      final int borderWidth;
      if (border != null) {
        int foo = 1;
        for (int i = 0;i < widths.length;i++) {
          if (widths[i] >= eltMinWidths[i]) {
            foo += widths[i] + 1;
          }
        }
        borderWidth = foo;
      } else {
        // Will not be used
        borderWidth = 0;
      }

      //
      return new LineReader() {

        /** The current height. */
        int height = 0;

        public boolean hasLine() {
          if (TableRenderer.this.height != null && height >= TableRenderer.this.height) {
            return false;
          } else {
            while (renderers.size() > 0) {
              Object first = renderers.peekFirst();
              if (first instanceof LineReader) {
                if (((LineReader)first).hasLine()) {
                  return true;
                } else {
                  renderers.removeFirst();
                }
              } else {
                return true;
              }
            }
            return false;
          }
        }
        public void renderLine(RenderAppendable to) {
          if (!hasLine()) {
            throw new IllegalStateException();
          }
          while (renderers.size() > 0) {
            Object first = renderers.peek();
            if (first instanceof LineReader) {
              if (((LineReader)first).hasLine()) {
                if (style != null) {
                  to.enterStyle(style);
                  ((LineReader)first).renderLine(to);
                  to.leaveStyle();
                } else {
                  ((LineReader)first).renderLine(to);
                }
                break;
              } else {
                renderers.removeFirst();
              }
            } else {
              Border border = (Border)first;
              renderers.removeFirst();
              to.styleOff();
              to.append(border.corner);
              for (int i = 0; i < borderWidth - 2;++i ) {
                to.append(border.horizontal);
              }
              to.append(border.corner);
              for (int i = borderWidth;i < width;i++) {
                to.append(' ');
              }
              to.styleOn();
              break;
            }
          }
          height++;
        }
      };
    }
  }
}
