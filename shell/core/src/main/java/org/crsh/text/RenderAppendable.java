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

package org.crsh.text;

import org.crsh.shell.ScreenContext;

import java.io.IOException;
import java.util.LinkedList;

public class RenderAppendable implements Appendable, ScreenContext {

  /** . */
  private final ScreenContext context;

  /** . */
  private LinkedList<Style.Composite> stack;

  public RenderAppendable(ScreenContext context) {
    this.context = context;
  }
  
  private void safeAppend(Chunk chunk) {
    try {
      context.write(chunk);
    }
    catch (java.io.IOException e) {
//      e.printStackTrace();
    }
  }

  public void write(Chunk chunk) throws IOException {
    safeAppend(chunk);
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public int getWidth() {
    return context.getWidth();
  }

  public int getHeight() {
    return context.getHeight();
  }

  public void flush() throws IOException {
    context.flush();
  }

  public RenderAppendable append(CharSequence csq) {
    safeAppend(Text.create(csq));
    return this;
  }

  public void enterStyle(Style.Composite style) {
    if (stack == null) {
      stack = new LinkedList<Style.Composite>();
    }
    safeAppend(style);
    stack.addLast(style);
  }

  public Style.Composite leaveStyle() {
    if (stack == null || stack.isEmpty()) {
      throw new IllegalStateException("Cannot leave non existing style");
    }
    Style.Composite last = stack.removeLast();
    if (stack.size() > 0) {

      // Compute merged
      Style.Composite merged = getMerged();

      // Compute diff with removed
      Boolean bold = foo(last.getBold(), merged.getBold());
      Boolean underline = foo(last.getUnderline(), merged.getUnderline());
      Boolean blink = foo(last.getBlink(), merged.getBlink());

      // For now we assume that black is the default background color
      // and white is the default foreground color
      Color fg = foo(last.getForeground(), merged.getForeground(), Color.def);
      Color bg = foo(last.getBackground(), merged.getBackground(), Color.def);

      //
      Style.Composite bilto = Style.style(bold, underline, blink, fg, bg);

      //   
      safeAppend(bilto);
    } else {
      safeAppend(Style.reset);
    }
    return last;
  }

  /**
   * Compute the current merged style.
   *
   * @return the merged style
   */
  private Style.Composite getMerged() {
    Style.Composite merged = Style.style();
    for (Style s : stack) {
      merged = (Style.Composite)merged.merge(s);
    }
    return merged;
  }

  private Boolean foo(Boolean last, Boolean merged) {
    if (last != null) {
      if (merged != null) {
        return merged;
      } else {
        return !last;
      }
    } else {
      return null;
    }
  }

  private Color foo(Color last, Color merged, Color def) {
    if (last != null) {
      if (merged != null) {
        return merged;
      } else {
        return def;
      }
    } else {
      return null;
    }
  }

  public void styleOff() {
    if (stack != null && stack.size() > 0) {
      safeAppend(Style.reset);
    }
  }

  public void styleOn() {
    if (stack != null && stack.size() > 0) {
      safeAppend(getMerged());
    }
  }

  public RenderAppendable append(CharSequence csq, int start, int end) {
    safeAppend(Text.create(csq.subSequence(start, end)));
    return this;
  }

  public RenderAppendable append(char c) {
    safeAppend(Text.create(Character.toString(c)));
    return this;
  }
}
