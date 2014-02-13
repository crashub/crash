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

import org.crsh.text.LineReader;
import org.crsh.text.LineRenderer;
import org.crsh.text.RenderAppendable;
import org.crsh.text.Style;

public abstract class Element {

  /** . */
  private Style.Composite style;

  protected Element() {
    this.style = null;
  }

  public abstract LineRenderer renderer();

  public void render(RenderAppendable to) {
    LineRenderer renderer = renderer();

    // For now height - 1 because of the char that goes to the line in some impl
    LineReader reader = renderer.reader(to.getWidth(), to.getHeight() - 1);
    if (reader != null) {
      while (reader.hasLine()) {
        reader.renderLine(to);
        to.append('\n');
      }
    }
  }

  public final Style.Composite getStyle() {
    return style;
  }

  public final void setStyle(Style.Composite style) {
    this.style = style;
  }

  public Element style(Style.Composite style) {
    setStyle(style);
    return this;
  }

  public static RowElement row() {
    return new RowElement();
  }

  public static RowElement header() {
    return new RowElement(true);
  }

  public static LabelElement label(String value) {
    return new LabelElement(value);
  }
}
