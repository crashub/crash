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

package org.crsh.text.ui;

import org.crsh.text.Color;
import org.crsh.text.ShellAppendable;
import org.crsh.text.Style;

import java.util.LinkedList;

public class RendererAppendable implements Appendable {

  /** . */
  private final ShellAppendable delegate;

  /** . */
  private LinkedList<Style.Composite> stack;

  public RendererAppendable(ShellAppendable delegate) {
    this.delegate = delegate;
  }

  public RendererAppendable append(CharSequence csq) {
    delegate.append(csq);
    return this;
  }

  public void enterStyle(Style.Composite style) {
    if (stack == null) {
      stack = new LinkedList<Style.Composite>();
    }
    delegate.append(style);
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
      delegate.append(bilto);
    } else {
      delegate.append(Style.reset);
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
      delegate.append(Style.reset);
    }
  }

  public void styleOn() {
    if (stack != null && stack.size() > 0) {
      delegate.append(getMerged());
    }
  }

  public RendererAppendable append(CharSequence csq, int start, int end) {
    delegate.append(csq, start, end);
    return this;
  }

  public RendererAppendable append(char c) {
    delegate.append(c);
    return this;
  }
}
