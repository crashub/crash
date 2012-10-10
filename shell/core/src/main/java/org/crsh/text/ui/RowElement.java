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

import org.crsh.text.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RowElement extends Element {

  /** . */
  private List<Element> cols;

  /** . */
  final boolean header;

  public RowElement() {
    this(false);
  }

  public RowElement(boolean header) {
    this.header = header;
    this.cols = new ArrayList<Element>();
  }

  @Override
  public Renderer renderer(int width) {
    return renderer(width, null);
  }

  public Renderer renderer(int width, Border border) {

    //
    int[] widths = new int[cols.size()];
    int[] minWidths = new int[cols.size()];
    for (int i = 0;i < cols.size();i++) {
      Element element = cols.get(i);
      widths[i] = Math.max(widths[i], element.getWidth());
      minWidths[i] = Math.max(minWidths[i], element.getMinWidth());
    }

    //
    widths = ColumnLayout.rightToLeft().compute(border, width, widths, minWidths);

    //
    if (widths == null) {
      return new Renderer() {
        public boolean hasLine() {
          return false;
        }
        public void renderLine(RendererAppendable to) throws IllegalStateException {
          throw new IllegalStateException();
        }
      };
    } else {
      return renderer(widths, width, border);
    }
  }

  public Renderer renderer(final int[] widths, final int viewWidth, final Border border) {
    final AtomicInteger totalWidth = new AtomicInteger();
    final Renderer[] renderers = new Renderer[cols.size()];
    for (int i = 0;i < cols.size();i++) {
      if (widths[i] > 0) {
        renderers[i] = cols.get(i).renderer(widths[i]);
        totalWidth.addAndGet(widths[i]);
      }
    }

    //
    return new Renderer() {
      public boolean hasLine() {
        for (Renderer renderer : renderers) {
          if (renderer != null) {
            if (renderer.hasLine()) {
              return true;
            }
          }
        }
        return false;
      }

      public void renderLine(RendererAppendable to) {
        int total = 0;
        if (border != null) {
          to.styleOff();
          to.append(border.vertical);
          to.styleOn();
          total++;
        }

        //
        Style.Composite style = getStyle();
        if (style != null) {
          to.enterStyle(style);
        }

        //
        for (int i = 0;i < renderers.length;i++) {
          Renderer renderer = renderers[i];
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
        while (total++ < viewWidth) {
          to.append(' ');
        }
      }
    };
  }

  @Override
  int getWidth() {
    int width = 0;
    for (Element col : cols) {
      width += col.getWidth();
    }
    return width;
  }

  @Override
  int getMinWidth() {
    int minWidth = 0;
    for (Element col : cols) {
      minWidth += col.getMinWidth();
    }
    return minWidth;
  }

  public RowElement add(Element... cols) {
    for (Element col : cols) {
      if (col.parent != null) {
        throw new IllegalArgumentException("Column has already a parent");
      }
    }
    for (Element col : cols) {
      this.cols.add(col);
      col.parent = this;
    }
    return this;
  }

  public List<Element> getCols() {
    return cols;
  }

  @Override
  public RowElement style(Style.Composite style) {
    return (RowElement)super.style(style);
  }
}
