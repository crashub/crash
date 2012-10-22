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

import org.crsh.text.Renderer;
import org.crsh.text.Style;

import java.util.ArrayList;
import java.util.List;

public class TableElement extends Element {

  /** . */
  ArrayList<RowElement> rows = new ArrayList<RowElement>();

  /** . */
  protected Border border;

  /** The column layout. */
  protected Layout columnLayout;

  /** The table height, null means no limit. */
  protected Integer height;

  public TableElement() {
    this.columnLayout = Layout.rightToLeft();
  }

  public TableElement(int ... columns) {
    this.columnLayout = Layout.weighted(columns);
  }

  public TableElement add(RowElement row) {
    if (row.parent != null) {
      throw new IllegalArgumentException("Row has already a parent");
    }
    rows.add(row);
    row.parent = this;
    return this;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) throws IllegalArgumentException {
    if (height != null && height < 0) {
      throw new IllegalArgumentException("No negative table height accepted");
    }
    this.height = height;
  }

  public Layout getColumnLayout() {
    return columnLayout;
  }

  public Border getBorder() {
    return border;
  }

  public Renderer renderer() {
    return new TableRenderer(this);
  }

  public TableElement withColumnLayout(Layout columnLayout) {
    this.columnLayout = columnLayout;
    return this;
  }

  public List<RowElement> getRows() {
    return rows;
  }

  public TableElement border(Border border) {
    setBorder(border);
    return this;
  }

  public void setBorder(Border border) {
    this.border = border;
  }

  @Override
  public TableElement style(Style.Composite style) {
    return (TableElement)super.style(style);
  }
}
