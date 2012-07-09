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
import org.crsh.text.Style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class RowElement extends Element {

  /** . */
  private List<Element> cols;

  public RowElement() {
    this.cols = new ArrayList<Element>();
  }

  @Override
  public void print(UIWriterContext ctx, ShellWriter writer) throws IOException {
    doPrint(ctx, writer);
  }

  @Override
  void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException {

    int i = 0;
    TableElement table = (TableElement) getParent();
    List<Integer> colsSize = table.getColsSize();

    for (Element e : cols) {

      //
      ctx.pad(writer);
      if (ctx.needLF) {
        writer.append("\n");
        ctx.parentUIContext.pad(writer);
      }
      ctx.stack.clear();
      ctx.padStyle = null;

      //
      e.print(ctx, writer);

      //
      ctx.padStyle = Style.style(e.getDecoration(), e.getForeground(), e.getBackground());
      for (int j = 0; j < colsSize.get(i) - e.width(); ++j) {
        ctx.stack.add(Pad.SPACE);
      }

      //
      ++i;
      ctx.needLF = false;

    }

    ctx.needLF = true;

  }

  @Override
  int width() {
    return 0;
  }

  public void addValue(Element element) {
    this.cols.add(element);
  }

  public List<Element> getValues() {
    return cols;
  }
  
}
