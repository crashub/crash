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
import org.crsh.text.RenderAppendable;
import org.crsh.text.Style;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class TableLineRenderer extends LineRenderer {

  /** . */
  final Layout columnLayout;

  /** . */
  final Layout rowLayout;

  /** . */
  final BorderStyle border;

  /** . */
  final BorderStyle separator;

  /** . */
  final Overflow overflow;

  /** . */
  final Style.Composite style;

  /** Cell padding left. */
  final int leftCellPadding;

  /** Cell padding right. */
  final int rightCellPadding;

  /** . */
  private TableRowLineRenderer head;

  /** . */
  private TableRowLineRenderer tail;

  TableLineRenderer(TableElement table) {
    this.rowLayout = table.getRowLayout();
    this.columnLayout = table.getColumnLayout();
    this.border = table.getBorder();
    this.style = table.getStyle();
    this.separator = table.getSeparator();
    this.overflow = table.getOverflow();
    this.leftCellPadding = table.getLeftCellPadding();
    this.rightCellPadding = table.getRightCellPadding();

    //
    for (RowElement row : table.getRows()) {
      if (head == null) {
        head = tail = new TableRowLineRenderer(this, row);
      } else {
        tail = tail.add(new TableRowLineRenderer(this, row));
      }
    }
  }

  private int getMaxColSize() {
    int n = 0;
    for (TableRowLineRenderer row = head;row != null;row = row.next()) {
      n = Math.max(n, row.getColsSize());
    }
    return n;
  }

  @Override
  public int getMinWidth() {
    int minWidth = 0;
    for (TableRowLineRenderer row = head;row != null;row = row.next()) {
      minWidth = Math.max(minWidth, row.getMinWidth());
    }
    return minWidth + (border != null ? 2 : 0);
  }

  @Override
  public int getActualWidth() {
    int actualWidth = 0;
    for (TableRowLineRenderer row = head;row != null;row = row.next()) {
      actualWidth = Math.max(actualWidth, row.getActualWidth());
    }
    return actualWidth + (border != null ? 2 : 0);
  }

  @Override
  public int getActualHeight(int width) {
    if (border != null) {
      width -= 2;
    }
    int actualHeight = 0;
    for (TableRowLineRenderer row = head;row != null;row = row.next()) {
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
    for (TableRowLineRenderer row = head;row != null;row = row.next()) {
      for (int i = 0;i < row.getCols().size();i++) {
        LineRenderer renderable = row.getCols().get(i);
        eltWidths[i] = Math.max(eltWidths[i], renderable.getActualWidth() + row.row.leftCellPadding + row.row.rightCellPadding);
        eltMinWidths[i] = Math.max(eltMinWidths[i], renderable.getMinWidth() + row.row.leftCellPadding + row.row.rightCellPadding);
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
        for (TableRowLineRenderer row = head;row != null;row = row.next()) {
          actualHeights[row.getIndex()] = row.getActualHeight(widths);
          minHeights[row.getIndex()] = row.getMinHeight(widths);
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
          for (TableRowLineRenderer row = head;row != null;row = row.next()) {
            if (row.getIndex() < heights.length) {
              int[] what;
              if (row.getColsSize() == widths.length) {
                what = widths;
              } else {

                // I'm not sure this algorithm is great
                // perhaps the space should be computed or some kind of merge
                // that respect the columns should be done

                // Redistribute space among columns
                what = new int[row.getColsSize()];
                for (int j = 0;j < widths.length;j++) {
                  what[j % what.length] += widths[j];
                }

                // Remove zero length columns to avoid issues
                int end = what.length;
                while (end > 0 && what[end - 1] == 0) {
                  end--;
                }

                //
                if (end != what.length) {
                  what = Arrays.copyOf(what, end);
                }
              }
              TableRowReader next = row.renderer(what, heights[row.getIndex()]);
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
      return LineRenderer.NULL.reader(width);
    }
  }
}
