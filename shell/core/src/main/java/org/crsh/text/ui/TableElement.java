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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TableElement extends Element {

  /** . */
  public final int MARGIN = 5;

  /** . */
  private ArrayList<RowElement> rows = new ArrayList<RowElement>();

  /** We set to null because it should be inited by the doPrint. */
  private int[] colSizes = null;

  /** . */
  protected Border border;

  /** The column layout. */
  protected ColumnLayout layout;

  public TableElement() {
    this.layout = ColumnLayout.rightToLeft();
  }

  public TableElement(int ... weights) {
    this.layout = ColumnLayout.weighted(weights);
  }

  public TableElement add(RowElement row) {
    if (row.parent != null) {
      throw new IllegalArgumentException("Row has already a parent");
    }
    rows.add(row);
    row.parent = this;
    return this;
  }

  public ColumnLayout getLayout() {
    return layout;
  }

  @Override
  public Renderer renderer(final int width) {

    int len = getMaxColSize();
    int[] eltWidths = new int[len];
    int[] eltMinWidths = new int[len];

    // Compute each column as is
    for (RowElement row : rows) {
      for (int i = 0;i < row.getCols().size();i++) {
        Element element = row.getCols().get(i);
        eltWidths[i] = Math.max(eltWidths[i], element.getWidth());
        eltMinWidths[i] = Math.max(eltMinWidths[i], element.getMinWidth());
      }
    }

    // Note that we may have a different widths != eltWidths according to the layout algorithm
    final int[] widths = layout.compute(border, width, eltWidths, eltMinWidths);

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
      final LinkedList<Object> renderers = new LinkedList<Object>();

      // Add all rows
      boolean prev = false;
      for (int i = 0;i < rows.size();i++) {
        RowElement row = rows.get(i);
        if (border != null && (row.header || i == 0) && !prev) {
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
        if (border != null && (row.header || i == rows.size() - 1)) {
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
      return new Renderer() {
        public boolean hasLine() {
          while (renderers.size() > 0) {
            Object first = renderers.peekFirst();
            if (first instanceof Renderer) {
              if (((Renderer)first).hasLine()) {
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
        public void renderLine(RendererAppendable to) {
          while (renderers.size() > 0) {
            Object first = renderers.peek();
            if (first instanceof Renderer) {
              if (((Renderer)first).hasLine()) {
                Style.Composite style = getStyle();
                if (style != null) {
                  to.enterStyle(style);
                  ((Renderer)first).renderLine(to);
                  to.leaveStyle();
                } else {
                  ((Renderer)first).renderLine(to);
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
        }
      };
    }
  }

  public TableElement layout(ColumnLayout layout) {
    this.layout = layout;
    return this;
  }

  public List<RowElement> getRows() {
    return rows;
  }

  public int[] getColSizes() {
    return colSizes;
  }

  private int[] computeColSizes(int width) {
    int columnNumber = getMaxColSize();
    int[] colSizes = new int[columnNumber];
    int colSum = 0;
    if (border != null) {
      colSum += 3;
    }
    for (int i = 0; i < columnNumber; ++i) {
      int colSize = 0;
      for (RowElement row : rows) {
        colSize = Math.max(colSize, row.getCols().get(i).getWidth() + MARGIN);
        int missingSpace = (colSum + colSize) - width;
        if (missingSpace > 0) {
          colSize -= missingSpace;
        }
      }
      colSizes[i] = colSize;
      colSum += colSize;
      if (border != null) {
        colSum += 2;
      }
    }
    return colSizes;
  }

  public TableElement border(Border border) {
    setBorder(border);
    return this;
  }

  public void setBorder(Border border) {
    this.border = border;
  }

  private int getMaxColSize() {
    int n = 0;
    for (RowElement row : rows) {
      n = Math.max(n, row.getCols().size());
    }
    return n;
  }

  @Override
  int getWidth() {
    int width = 0;
    for (RowElement row : rows) {
      width = Math.max(width, row.getWidth());
    }
    return width;
  }

  @Override
  public TableElement style(Style.Composite style) {
    return (TableElement)super.style(style);
  }
}
