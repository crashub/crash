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

class TableRowReader implements LineReader {

  /** . */
  private final TableRowLineRenderer renderer;

  /** . */
  private final int[] widths;

  /** . */
  private final RowLineRenderer row;

  /** . */
  private LineReader reader;

  /** . */
  private TableRowReader previous;

  /** . */
  private TableRowReader next;

  /** . */
  private BorderStyle top;

  /** . */
  private BorderStyle bottom;

  /** . */
  private final int height;

  /**
   * 0 -> render top
   * 1 -> render cells
   * 2 -> render bottom
   * 3 -> done
   */
  private int status;

  TableRowReader(TableRowLineRenderer renderer, RowLineRenderer row, int[] widths, int height) {

    //
    this.renderer = renderer;
    this.row = row;
    this.widths = widths;
    this.reader = null;
    this.top = null;
    this.bottom = null;
    this.height = height;
    this.status = 1;
  }

  TableRowReader add(TableRowReader next) {
    next.previous = this;
    this.next = next;
    bottom = renderer.header ? (renderer.table.separator != null ? renderer.table.separator : BorderStyle.DASHED) : null;
    next.top = next.renderer.header && !renderer.header ? (next.renderer.table.separator != null ? next.renderer.table.separator : BorderStyle.DASHED) : null;
    next.status = next.top != null ? 0 : 1;
    return next;
  }

  TableRowReader previous() {
    return previous;
  }

  TableRowReader next() {
    return next;
  }

  boolean hasTop() {
    return renderer.header && previous != null;
  }

  boolean hasBottom() {
    return renderer.header && next != null && !next.renderer.header;
  }

  boolean isSeparator() {
    return status == 0 || status == 2;
  }

  public boolean hasLine() {
    return 0 <= status && status <= 2;
  }

  public void renderLine(RenderAppendable to) throws IllegalStateException {
    if (!hasLine()) {
      throw new IllegalStateException();
    }
    switch (status) {
      case 0:
      case 2: {
        BorderStyle b = status == 0 ? top : bottom;
        to.styleOff();
        for (int i = 0;i < widths.length;i++) {
          if (i > 0 && renderer.table.separator != null) {
            to.append(b.horizontal);
          }
          for (int j = 0;j < widths[i];j++) {
            to.append(b.horizontal);
          }
        }
        to.styleOn();
        status++;
        break;
      }
      case 1: {

        //
        if (reader == null) {
          if (height > 0 && renderer.table.overflow == Overflow.WRAP) {
            int h = height;
            if (hasTop()) {
              h--;
            }
            if (hasBottom()) {
              h--;
            }
            reader = row.renderer(widths, h);
          } else {
            reader = row.renderer(widths, -1);
          }
        }

        //
        reader.renderLine(to);

        //
        if (renderer.table.overflow == Overflow.HIDDEN) {
          status = bottom != null ? 2 : 3;
        } else {
          if (!reader.hasLine()) {
            status = bottom != null ? 2 : 3;
          }
        }

        //
        break;
      }
      default:
        throw new AssertionError();
    }
  }
}
