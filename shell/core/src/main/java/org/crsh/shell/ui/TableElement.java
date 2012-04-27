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
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class TableElement extends Element {

  /** . */
  private final int MARGIN = 5;

  /** . */
  private List<Integer> colsSize = new ArrayList<Integer>();

  /** . */
  private List<RowElement> rows = new ArrayList<RowElement>();

  public TableElement addRow(RowElement row) {

    //
    int remainingSpace = 235;
    row.setTable(this);
    int i = 0;
    for (String value : row.getValues()) {
      if (colsSize.size() <= i) {
        int usedSpace = Math.min(remainingSpace - MARGIN, value.length());
        remainingSpace -= usedSpace + MARGIN;
        colsSize.add(usedSpace);
      }
      else {
        int max = Math.max(value.length(), colsSize.get(i));
        int usedSpace = Math.min(max, remainingSpace - MARGIN);
        remainingSpace -= usedSpace + MARGIN;
        colsSize.set(i, usedSpace);
      }
      ++i;
    }

    //
    rows.add(row);
    return this;
  }
  
  @Override
  void print(UIWriterContext ctx, ShellWriter writer) throws IOException {

    if (ctx == null) {
      ctx = new UIWriterContext();
    }

    for (RowElement e : rows) {
      e.print(ctx, writer);
    }
  }

  public List<Integer> getColsSize() {
    return colsSize;
  }
  
}
