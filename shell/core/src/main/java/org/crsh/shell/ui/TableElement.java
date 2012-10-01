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

package org.crsh.shell.ui;

import org.crsh.shell.io.ShellFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableElement extends Element {

  /** . */
  public final int MARGIN = 5;

  /** . */
  private List<RowElement> rows = new ArrayList<RowElement>();

  /** . */
  private List<Integer> colsSize = new ArrayList<Integer>();

  /** . */
  protected Border border;

  public TableElement add(RowElement row) {
    if (row.parent != null) {
      throw new IllegalArgumentException("Row has already a parent");
    }
    rows.add(row);
    row.parent = this;
    return this;
  }

  public void printLine(int length, ShellFormatter writer) {
    writer.append(border.corner);
    for (int i = 0; i < length; ++i ) {
      writer.append(border.horizontal);
    }
    writer.append(border.corner);
    writer.append('\n');
  }


  @Override
  void doPrint(UIWriterContext ctx, ShellFormatter writer) {

    //
    ctx = new UIWriterContext(ctx);
    colsSize = computeColSize(ctx.getConsoleWidth());

    // Print top line
    if (border != null) {
      ctx.parentUIContext.pad(writer);
      printLine(width() - 2, writer);
    }

    //
    ctx.parentUIContext.pad(writer);
    for (RowElement e : rows) {
      e.print(ctx, writer);
    }
    ctx.pad(writer);

    //
    if (border != null) {
      writer.append(border.vertical);
    }
    writer.append("\n");

    // Print bottom line
    if (border != null) {
      ctx.parentUIContext.pad(writer);
      printLine(width() - 2, writer);
    }

  }

  public List<RowElement> getRows() {
    return rows;
  }

  public List<Integer> getColsSize() {
    return Collections.unmodifiableList(colsSize);
  }

  private List<Integer> computeColSize(int consoleWidth) {

    List<Integer> colsSize = new ArrayList<Integer>();

    int colSum = 0;
    if (border != null) {
        colSum += 3;
      }
    for (int i = 0; i < columnNumber(); ++i) {
      int colSize = 0;
      for (RowElement row : rows) {
        colSize = Math.max(colSize, row.getCols().get(i).width() + MARGIN);
        int missingSpace = (colSum + colSize) - consoleWidth;
        if (missingSpace > 0) {
          colSize -= missingSpace;
        }
      }
      colsSize.add(colSize);
      colSum += colSize;
      if (border != null) {
        colSum += 2;
      }
    }

    return colsSize;
    
  }

  public TableElement border(Border border) {
    setBorder(border);
    return this;
  }

  public void setBorder(Border border) {
    this.border = border;
  }

  private int columnNumber() {

    int n = 0;

    for (RowElement row : rows) {
      n = Math.max(n, row.getCols().size());
    }

    return n;

  }

  @Override
  int width() {

    //
    int sum = 0;
    for (int colSize : colsSize) sum += colSize;
    if (border != null) {
      sum += (colsSize.size() * 2) + 1;
    }

    //
    return sum;

  }
}
