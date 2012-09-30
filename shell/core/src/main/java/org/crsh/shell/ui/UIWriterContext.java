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

import org.crsh.command.InvocationContext;
import org.crsh.shell.io.ShellFormatter;
import org.crsh.shell.io.ShellWriterContext;
import org.crsh.text.Style;

import java.util.ArrayList;
import java.util.Collections;

class UIWriterContext implements ShellWriterContext {

  /** . */
  final ArrayList<Pad> stack;

  /** . */
  Style padStyle;

  /** . */
  boolean needLF;

  /** . */
  boolean needLine;

  /** . */
  private final InvocationContext context;

  /** . */
  String leftLinePadding = "";

  /** . */
  String rightLinePadding = "";

  /** . */
  UIWriterContext parentUIContext;

  UIWriterContext(InvocationContext context) {
    this.context = context;
    this.stack = new ArrayList<Pad>();
    this.needLF = false;
  }

  UIWriterContext(UIWriterContext parentUIContext) {
    this(parentUIContext.getInvocationContext());
    this.parentUIContext = parentUIContext;
  }

  public void pad(ShellFormatter writer) {
    for (int i = 0;i < stack.size();i++) {
      Pad abc = stack.get(i);
      
      //
      if (padStyle != null) {
        new FormattingElement(padStyle).print(this, writer);
      }

      //
      writer.append(abc.chars);

      //
      if (padStyle != null) {
        new FormattingElement(Style.reset).print(this, writer);
      }

      Pad next = abc.next();
      stack.set(i, next);
    }
    stack.removeAll(Collections.singleton(null));
  }

  public void text(CharSequence csq, int off, int end) {
    needLF = true;
  }

  public void lineFeed() {
    needLF = false;
  }

  public int getConsoleWidth() {
    return context.getWidth();
  }

  public InvocationContext getInvocationContext() {
    return context;
  }

  public int padWidth() {
    int width = leftLinePadding.length() + rightLinePadding.length();
    for (Pad pad : stack) {
      String p = pad.chars;
      if (p != null) {
        width += p.length();
      }
    }
    return width;
  }
}
