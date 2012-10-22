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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class TableRenderer extends Renderer {

  /** . */
  private final Layout columnLayout;

  /** . */
  private final Layout rowLayout;

  /** . */
  private final BorderStyle border;

  /** . */
  private final BorderStyle separator;

  /** . */
  private final Style.Composite style;

  /** . */
  private TableRowRenderer head;

  /** . */
  private TableRowRenderer tail;

  TableRenderer(TableElement table) {
    for (RowElement row : table.getRows()) {
      if (head == null) {
        head = tail = new TableRowRenderer(row.renderer(), row.header);
      } else {
        tail = tail.add(new TableRowRenderer(row.renderer(), row.header));
      }
    }

    //
    this.rowLayout = table.getRowLayout();
    this.columnLayout = table.getColumnLayout();
    this.border = table.getBorder();
    this.style = table.getStyle();
    this.separator = table.getSeparator();
  }

  private int getMaxColSize() {
    int n = 0;
    for (TableRowRenderer row = head;row != null;row = row.next()) {
      n = Math.max(n, row.getColsSize());
    }
    return n;
  }

  @Override
  public int getMinWidth() {
    int width = 0;
    for (TableRowRenderer row = head;row != null;row = row.next()) {
      width = Math.max(width, row.getMinWidth());
    }
    return width;
  }

  @Override
  public int getActualWidth() {
    int width = 0;
    for (TableRowRenderer row = head;row != null;row = row.next()) {
      width = Math.max(width, row.getActualWidth());
    }
    return width;
  }

  @Override
  public int getActualHeight(int width) {
    if (border != null) {
      width -= 2;
    }
    int actualHeight = 0;
    for (TableRowRenderer row = head;row != null;row = row.next()) {
      actualHeight += row.getActualHeight(width);
    }
    if (border != null) {
      actualHeight += 2;
    }
    return actualHeight;
  }

  @Override
  public int getMinHeight(int width) {
    return border != null ? 2 : 0;
  }

  @Override
  public LineReader reader(int width) {
    return reader(width, 0);
  }

  @Override
  public LineReader reader(final int width, final int height) {

    int len = getMaxColSize();
    int[] eltWidths = new int[len];
    int[] eltMinWidths = new int[len];

    // Compute each column as is
    for (TableRowRenderer row = head;row != null;row = row.next()) {
      for (int i = 0;i < row.getCols().size();i++) {
        Renderer renderable = row.getCols().get(i);
        eltWidths[i] = Math.max(eltWidths[i], renderable.getActualWidth());
        eltMinWidths[i] = Math.max(eltMinWidths[i], renderable.getMinWidth());
      }
    }

    // Note that we may have a different widths != eltWidths according to the layout algorithm
    final int[] widths = columnLayout.compute(separator != null, width - (border != null ? 2 : 0), eltWidths, eltMinWidths);

    //
    if (widths != null) {
      // Compute new widths array
      final AtomicInteger effectiveWidth = new AtomicInteger();
      if (border != null) {
        effectiveWidth.addAndGet(2);
      }
      for (int i = 0;i < widths.length;i++) {
        effectiveWidth.addAndGet(widths[i]);
        if (separator != null) {
          if (i > 0) {
            effectiveWidth.addAndGet(1);
          }
        }
      }

      //
      final int[] heights;
      if (height > 0) {
        // Apply vertical layout
        int size = tail.getSize();
        int[] actualHeights = new int[size];
        int[] minHeights = new int[size];
        for (TableRowRenderer row = head;row != null;row = row.next()) {

          int actualHeight = 0;
          int minHeight = 0;
          for (int i = 0;i < widths.length;i++) {
            Renderer col = row.row.getCols().get(i);
            actualHeight = Math.max(actualHeight, col.getActualHeight(widths[i]));
            minHeight = Math.max(minHeight, col.getMinHeight(widths[i]));
          }

          //
          if (row.hasTop()) {
            actualHeight++;
            minHeight++;
          }
          if (row.hasBottom()) {
            actualHeight++;
            minHeight++;
          }

          //
          actualHeights[row.getIndex()] = actualHeight;
          minHeights[row.getIndex()] = minHeight;
        }
        heights = rowLayout.compute(false, height - (border != null ? 2 : 0), actualHeights, minHeights);
        if (heights == null) {
          return null;
        }
      } else {
        heights = new int[tail.getSize()];
        Arrays.fill(heights, -1);
      }

      //
      return new LineReader() {

        /** . */
        TableRowReader rHead = null;

        /** . */
        TableRowReader rTail = null;

        /** . */
        int index = 0;

        /**
         * 0 -> render top
         * 1 -> render rows
         * 2 -> render bottom
         * 3 -> done
         */
        int status = border != null ? 0 : 1;

        {
          // Add all rows
          for (TableRowRenderer row = head;row != null;row = row.next()) {
            if (row.getIndex() < heights.length) {
              int[] what;
              if (row.getColsSize() == widths.length) {
                what = widths;
              } else {
                // Redistribute space among columns
                what = new int[row.getColsSize()];
                for (int j = 0;j < widths.length;j++) {
                  what[j % what.length] += widths[j];
                }
              }
              TableRowReader next = row.renderer(what, separator, heights[row.getIndex()]);
              if (rHead == null) {
                rHead = rTail = next;
              } else {
                rTail = rTail.add(next);
              }
            } else {
              break;
            }
          }
        }

        public boolean hasLine() {
          switch (status) {
            case 0:
            case 2:
              return true;
            case 1:
              while (rHead != null) {
                if (rHead.hasLine()) {
                  return true;
                } else {
                  rHead = rHead.next();
                }
              }

              // Update status according to height
              if (height > 0) {
                if (border == null) {
                  if (index == height) {
                    status = 3;
                  }
                } else {
                  if (index == height - 1) {
                    status = 2;
                  }
                }
              } else {
                if (border != null) {
                  status = 2;
                } else {
                  status = 3;
                }
              }

              //
              return status < 3;
            default:
              return false;
          }
        }

        public void renderLine(RenderAppendable to) {
          if (!hasLine()) {
            throw new IllegalStateException();
          }
          switch (status) {
            case 0:
            case 2: {
              to.styleOff();
              to.append(border.corner);
              for (int i = 0;i < widths.length;i++) {
                if (widths[i] > 0) {
                  if (separator != null && i > 0) {
                    to.append(border.horizontal);
                  }
                  for (int j = 0;j < widths[i];j++) {
                    to.append(border.horizontal);
                  }
                }
              }
              to.append(border.corner);
              to.styleOn();
              for (int i = width - effectiveWidth.get();i > 0;i--) {
                to.append(' ');
              }
              status++;
              break;
            }
            case 1: {

              //
              boolean sep = rHead != null && rHead.isSeparator();
              if (border != null) {
                to.styleOff();
                to.append(sep ? border.corner : border.vertical);
                to.styleOn();
              }

              //
              if (style != null) {
                to.enterStyle(style);
              }

              //
              if (rHead != null) {
                // Render row
                rHead.renderLine(to);
              } else {
                // Vertical padding
                for (int i = 0;i < widths.length;i++) {
                  if (separator != null && i > 0) {
                    to.append(separator.vertical);
                  }
                  for (int j = 0;j < widths[i];j++) {
                    to.append(' ');
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
                to.append(sep ? border.corner : border.vertical);
                to.styleOn();
              }

              // Padding
              for (int i = width - effectiveWidth.get();i > 0;i--) {
                to.append(' ');
              }
              break;
            }
            default:
              throw new AssertionError();
          }

          // Increase vertical index
          index++;
        }
      };
    } else {
      return Renderer.NULL.reader(width);
    }
  }
}
