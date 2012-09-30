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
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

import java.util.ArrayList;
import java.util.List;

public class RowElement extends Element {

  /** . */
  private List<Element> cols;

  /** . */
  final boolean header;

  public RowElement() {
    this(false);
  }

  public RowElement(boolean header) {
    this.header = header;
    this.cols = new ArrayList<Element>();
  }

  @Override
  public void print(UIWriterContext ctx, ShellFormatter writer) {
    doPrint(ctx, writer);
  }

  @Override
  void doPrint(UIWriterContext ctx, ShellFormatter writer) {

    int i = 0;
    TableElement table = (TableElement)parent;
    List<Integer> colsSize = table.getColsSize();

    // Request bottom header line
    if (table.border != null && header) {
      ctx.needLine = header;
    }

    // Init line padding
    if (table.border != null) {
      ctx.leftLinePadding += table.border.vertical + " ";
      ctx.rightLinePadding += table.border.vertical;
    }

    for (Element e : cols) {

      //
      int availableWidth = (ctx.getConsoleWidth() - ctx.leftLinePadding.length() - ctx.rightLinePadding.length());
      if (availableWidth <= 0) {
        break;
      }

      //
      ctx.pad(writer);
      if (ctx.needLF) {

        //
        if (table.border != null) {
          writer.append(table.border.vertical);
        }
        writer.append("\n");

        //
        ctx.parentUIContext.pad(writer);
        if (table.border != null && ctx.needLine) {
          table.printLine(table.width() - 2, writer);
          ctx.parentUIContext.pad(writer);
        }

      }
      ctx.padStyle = null;

      //
      if (table.border != null) {
        writer.append(table.border.vertical);
        ctx.stack.add(Pad.SPACE);
        ctx.padStyle = Style.style(e.getDecoration(), e.getForeground(), e.getBackground());
        ctx.pad(writer);
      }

      //
      e.print(ctx, writer);
      ctx.stack.clear();

      //
      ctx.padStyle = Style.style(e.getDecoration(), e.getForeground(), e.getBackground());
      for (int j = 0; j < colsSize.get(i) - e.width(); ++j) {
        ctx.stack.add(Pad.SPACE);
      }

      //
      for (int index = 0; index < table.getColsSize().get(i); ++index) {
        ctx.leftLinePadding += " ";
      }
      if (table.border != null) {
        ctx.leftLinePadding += table.border.vertical + " ";
      }

      //
      ++i;
      ctx.needLF = false;
      ctx.needLine = false;

    }

    //
    ctx.needLF = true;
    ctx.needLine = header;
    ctx.leftLinePadding = "";
    ctx.rightLinePadding = "";

  }

  @Override
  int width() {
    return 0;
  }

  public RowElement add(Element col) {
    if (col.parent != null) {
      throw new IllegalArgumentException("Column has already a parent");
    }
    this.cols.add(col);
    col.parent = this;
    return this;
  }

  public List<Element> getCols() {
    return cols;
  }

  @Override
  public RowElement decoration(Decoration decoration) {
    return (RowElement)super.decoration(decoration);
  }

  @Override
  public RowElement foreground(Color foreground) {
    return (RowElement)super.foreground(foreground);
  }

  @Override
  public RowElement background(Color background) {
    return (RowElement)super.background(background);
  }
}
