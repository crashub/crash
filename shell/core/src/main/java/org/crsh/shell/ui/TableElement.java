/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.crsh.shell.io.ShellWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class TableElement extends Element {

  /** . */
  public final int MARGIN = 5;

  /** . */
  private List<RowElement> rows = new ArrayList<RowElement>();

  /** . */
  private List<Integer> colsSize = new ArrayList<Integer>();

  public TableElement addRow(RowElement row) {
    rows.add(row);
    return this;
  }
  
  @Override
  void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException {

    ctx = new UIWriterContext(ctx);

    colsSize = computeColSize(ctx.getConsoleWidth());
    ctx.parentUIContext.pad(writer);
    for (RowElement e : rows) {
      e.print(ctx, writer);
    }

    ctx.pad(writer);
    writer.append("\n");
    
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
    for (int i = 0; i < columnNumber(); ++i) {
      int colSize = 0;
      for (RowElement row : rows) {
        colSize = Math.max(colSize, row.getValues().get(i).width() + MARGIN);
        int missingSpace = (colSum + colSize) - consoleWidth;
        if (missingSpace > 0) {
          colSize -= missingSpace;
        }
      }
      colsSize.add(colSize);
      colSum += colSize;
    }

    return colsSize;
    
  }

  private int columnNumber() {

    int n = 0;

    for (RowElement row : rows) {
      n = Math.max(n, row.getValues().size());
    }

    return n;

  }

  @Override
  int width() {
    return 0;
  }
}
