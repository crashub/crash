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
public class RowElement extends Element {

  /** . */
  private TableElement table = new TableElement();

  /** . */
  private List<LabelElement> cols;

  public RowElement(List<LabelElement> cols) {
    this.cols = cols;
  }

  public void setTable(TableElement table) {
    this.table = table;
  }

  @Override
  void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException {

    int i = 0;
    for (LabelElement e : cols) {

      // Right padding
      int padSize = table.getColsSize().get(i) - e.width();
      StringBuilder sb = new StringBuilder();
      for (int _ = 0; _ < padSize + table.MARGIN; ++_) {
        sb.append(" ");
      }
      ctx.rightPad = sb.toString();
      ++i;
      
      //
      e.print(ctx, writer);
    }
    writer.append(ctx, '\n');

    ctx.rightPad = null;
  }

  @Override
  int width() {
    int width = 0;
    for (LabelElement e : cols) {
      width += e.width();
    }
    return width + (cols.size() * table.MARGIN);
  }

  public List<LabelElement> getValues() {
    return cols;
  }
  
}
