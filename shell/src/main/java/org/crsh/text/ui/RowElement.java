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

import org.crsh.text.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RowElement extends Element {

  /** . */
  List<Element> cols;

  /** . */
  final boolean header;

  public RowElement() {
    this(false);
  }

  public RowElement(boolean header) {
    this.cols = new ArrayList<Element>();
    this.header = header;
  }

  public int getSize() {
    return cols.size();
  }

  public Element getCol(int index) {
    return cols.get(index);
  }

  public RowElement add(Element... cols) {
    Collections.addAll(this.cols, cols);
    return this;
  }

  public RowElement add(String... cols) {
    for (String col : cols) {
      this.cols.add(new LabelElement(col));
    }
    return this;
  }

  @Override
  public RowElement style(Style.Composite style) {
    return (RowElement)super.style(style);
  }

  @Override
  public RowLineRenderer renderer() {
    return new RowLineRenderer(this, null, 0, 0);
  }
}
