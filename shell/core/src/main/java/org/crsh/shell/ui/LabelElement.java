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

import org.crsh.shell.io.ShellWriter;

import java.io.IOException;

public class LabelElement extends Element {

  /** . */
  private final String value;

  public LabelElement(String value) {
    this.value = value;
  }

  public LabelElement(Object value) {
    this.value = String.valueOf(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException {

    //
    int availableWidth = (ctx.getConsoleWidth() - ctx.padWidth());
    
    if (availableWidth < value.length()) {

      String remainingText = value;
      boolean first = true;

      while (availableWidth < remainingText.length()) {

        //
        String currentValue = remainingText.substring(0, availableWidth).trim();
        remainingText = remainingText.substring(availableWidth).trim();

        //
        printLine(ctx, writer, currentValue, availableWidth, !first);
        writer.append(ctx, "\n");

        //
        first = false;
        
      }

      //
      ctx.rightLinePadding = "";
      printLine(ctx, writer, remainingText, availableWidth, !first);

    } else {
      writer.append(ctx, value);
    }
  }

  private void printLine(UIWriterContext ctx, ShellWriter writer, String value, int available, boolean needed) throws IOException {

    //
    if (needed) {
      if (ctx.parentUIContext != null) {
        ctx.parentUIContext.pad(writer);
      }
      ctx.pad(writer);
      writer.append(ctx, ctx.leftLinePadding);
    }

    //
    writer.append(ctx, value);

    //
    for (int pos = value.length(); pos < available; ++pos) {
      writer.append(" ");
    }
    writer.append(ctx, ctx.rightLinePadding);

  }

  @Override
  public String toString() {
    return "Label[" + value + "]";
  }

  @Override
  int width() {
    return value.length();
  }

}
