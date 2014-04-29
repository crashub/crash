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

import java.util.List;

class TableRowLineRenderer {

  /** . */
  final TableLineRenderer table;

  /** . */
  final RowLineRenderer row;

  /** . */
  final boolean header;

  /** . */
  private TableRowLineRenderer previous;

  /** . */
  private TableRowLineRenderer next;

  /** . */
  private int index;

  TableRowLineRenderer(TableLineRenderer table, RowElement row) {
    this.table = table;
    this.row = new RowLineRenderer(row, table.separator, table.leftCellPadding, table.rightCellPadding);
    this.header = row.header;
    this.index = 0;
  }

  TableRowLineRenderer add(TableRowLineRenderer next) {
    next.previous = this;
    next.index = index + 1;
    this.next = next;
    return next;
  }

  boolean hasTop() {
    return header && previous != null;
  }

  boolean hasBottom() {
    return header && next != null && !next.header;
  }

  int getIndex() {
    return index;
  }

  int getSize() {
    return index + 1;
  }

  TableRowLineRenderer previous() {
    return previous;
  }

  TableRowLineRenderer next() {
    return next;
  }

  boolean isHeader() {
    return header;
  }

  int getColsSize() {
    return row.getSize();
  }

  List<LineRenderer> getCols() {
    return row.getCols();
  }

  int getActualWidth() {
    return row.getActualWidth();
  }

  int getMinWidth() {
    return row.getMinWidth();
  }

  int getActualHeight(int width) {
    int actualHeight;
    switch (table.overflow) {
      case HIDDEN:
        actualHeight = 1;
        break;
      case WRAP:
        actualHeight = row.getActualHeight(width);
        break;
      default:
        throw new AssertionError();
    }
    if (hasTop()) {
      actualHeight++;
    }
    if (hasBottom()) {
      actualHeight++;
    }
    return actualHeight;
  }

  int getActualHeight(int[] widths) {
    int actualHeight;
    switch (table.overflow) {
      case HIDDEN:
        actualHeight = 1;
        break;
      case WRAP:
        actualHeight = 0;
        for (int i = 0;i < widths.length;i++) {
          LineRenderer col = row.getCols().get(i);
          actualHeight = Math.max(actualHeight, col.getActualHeight(widths[i]));
        }
        break;
      default:
        throw new AssertionError();
    }
    if (hasTop()) {
      actualHeight++;
    }
    if (hasBottom()) {
      actualHeight++;
    }
    return actualHeight;
  }

  int getMinHeight(int[] widths) {
    int minHeight;
    switch (table.overflow) {
      case HIDDEN:
        minHeight = 1;
        break;
      case WRAP:
        minHeight = 0;
        for (int i = 0;i < widths.length;i++) {
          LineRenderer col = row.getCols().get(i);
          minHeight = Math.max(minHeight, col.getMinHeight(widths[i]));
        }
        break;
      default:
        throw new AssertionError();
    }
    if (hasTop()) {
      minHeight++;
    }
    if (hasBottom()) {
      minHeight++;
    }
    return minHeight;
  }

  TableRowReader renderer(int[] widths, int height) {
    return new TableRowReader(this, row, widths, height);
  }
}
