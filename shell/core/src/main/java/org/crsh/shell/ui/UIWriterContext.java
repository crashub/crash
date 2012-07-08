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

import org.crsh.command.InvocationContext;
import org.crsh.shell.io.ShellWriter;
import org.crsh.shell.io.ShellWriterContext;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class UIWriterContext implements ShellWriterContext {

  /** . */
  final ArrayList<Pad> stack;

  /** . */
  Style padStyle;

  /** . */
  boolean needLF;

  /** . */
  private final InvocationContext context;

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

  private static EnumMap<Pad, String> charMap = new EnumMap<Pad, String>(Pad.class);
  private static EnumMap<Pad, Pad> nextMap = new EnumMap<Pad, Pad>(Pad.class);

  static {
    charMap.put(Pad.BRANCH, "+-");
    charMap.put(Pad.LAST_BRANCH, "+-");
    charMap.put(Pad.CONTINUE_BRANCH, "| ");
    charMap.put(Pad.STOP_BRANCH, "  ");
    charMap.put(Pad.SPACE, " ");
    nextMap.put(Pad.BRANCH, Pad.CONTINUE_BRANCH);
    nextMap.put(Pad.CONTINUE_BRANCH, Pad.CONTINUE_BRANCH);
    nextMap.put(Pad.LAST_BRANCH, Pad.STOP_BRANCH);
    nextMap.put(Pad.STOP_BRANCH, Pad.STOP_BRANCH);
  }

  public void pad(ShellWriter writer) throws IOException {
    for (int i = 0;i < stack.size();i++) {
      Pad abc = stack.get(i);
      
      //
      if (padStyle != null) {
        new FormattingElement(padStyle).print(this, writer);
      }

      //
      writer.append(charMap.get(abc));

      //
      if (padStyle != null) {
        new FormattingElement(null).print(this, writer);
      }

      Pad next = nextMap.get(abc);
      stack.set(i, next);
    }
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
  
}
