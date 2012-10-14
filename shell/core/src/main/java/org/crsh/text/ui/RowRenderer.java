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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class RowRenderer extends Renderer {

  /** . */
  private final List<Renderer> cols;

  /** . */
  private final Style.Composite style;

  /** . */
  private final boolean header;

  RowRenderer(RowElement row) {

    List<Renderer> cols = new ArrayList<Renderer>(row.getCols().size());
    for (Element col : row.getCols()) {
      cols.add(col.renderer());
    }

    //
    this.cols = cols;
    this.style = row.getStyle();
    this.header = row.header;
  }

  public boolean isHeader() {
    return header;
  }

  int getSize() {
    return cols.size();
  }

  public List<Renderer> getCols() {
    return cols;
  }

  public LineReader renderer(final int width, Border border) {

    //
    int[] widths = new int[cols.size()];
    int[] minWidths = new int[cols.size()];
    for (int i = 0;i < cols.size();i++) {
      Renderer renderable = cols.get(i);
      widths[i] = Math.max(widths[i], renderable.getActualWidth());
      minWidths[i] = Math.max(minWidths[i], renderable.getMinWidth());
    }

    //
    widths = ColumnLayout.rightToLeft().compute(border, width, widths, minWidths);

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
      return renderer(widths, width, border);
    }
  }

  public LineReader renderer(final int[] widths, final int width, final Border border) {
    final AtomicInteger totalWidth = new AtomicInteger();
    final LineReader[] renderers = new LineReader[cols.size()];
    for (int i = 0;i < cols.size();i++) {
      if (widths[i] > 0) {
        renderers[i] = cols.get(i).renderer(widths[i]);
        totalWidth.addAndGet(widths[i]);
      }
    }

    //
      return new LineReader() {
      public boolean hasLine() {
        for (LineReader renderer : renderers) {
          if (renderer != null) {
            if (renderer.hasLine()) {
              return true;
            }
          }
        }
        return false;
      }

      public void renderLine(RenderAppendable to) {
        int total = 0;
        if (border != null) {
          to.styleOff();
          to.append(border.vertical);
          to.styleOn();
          total++;
        }

        //
        if (style != null) {
          to.enterStyle(style);
        }

        //
        for (int i = 0;i < renderers.length;i++) {
          LineReader renderer = renderers[i];
          if (widths[i] > 0) {
            if (i > 0) {
              if (border != null) {
                to.styleOff();
                to.append(border.vertical);
                to.styleOn();
                total++;
              }
            }

            total += widths[i];
            if (renderer != null && renderer.hasLine()) {
              renderer.renderLine(to);
            } else {
              renderers[i] = null;
              for (int j = widths[i];j > 0;j--) {
                to.append(' ');
              }
            }

          }
        }

        //
        if (style != null) {
          to.leaveStyle();
        }

        //
        if (border != null) {
          to.styleOff();
          to.append(border.vertical);
          to.styleOff();
          total++;
        }
        while (total++ < width) {
          to.append(' ');
        }
      }
    };
  }

  @Override
  public LineReader renderer(int width) {
    return renderer(width, null);
  }

  @Override
  public int getActualWidth() {
    int width = 0;
    for (Renderer col : cols) {
      width += col.getActualWidth();
    }
    return width;
  }

  @Override
  public int getMinWidth() {
    int minWidth = 0;
    for (Renderer col : cols) {
      minWidth += col.getMinWidth();
    }
    return minWidth;
  }
}
