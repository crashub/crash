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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class RowElement extends Element {

  /** . */
  private TableElement table = new TableElement();

  /** . */
  private List<String> values = new ArrayList<String>();

  /** . */
  private List<Style> styles = new ArrayList<Style>();

  public RowElement(List<Object> values, List<Style> styles) {
    this.styles = styles;
    this.values = new ArrayList<String>();
    for (Object o : values) {
      this.values.add(o.toString());
    }
  }

  public void setTable(TableElement table) {
    this.table = table;
  }

  @Override
  void print(UIWriterContext ctx, ShellWriter writer) throws IOException {

    int i = 0;
    for (String value : values) {

      if (styles != null && styles.size() > 0) {
        if (styles.size() <= i) {
          new FormattingElement(styles.get(styles.size() - 1)).print(writer);
        } else {
          new FormattingElement(styles.get(i)).print(writer);
        }
      }

      int padSize = table.getColsSize().get(i) - value.length();
      StringBuilder sb = new StringBuilder(value);
      for (int _ = 0; _ < padSize + 5; ++_) {
        sb.append(" ");
      }
      writer.append(ctx, sb.toString());
      ++i;

      if (styles != null && styles.size() > 0) {
        new FormattingElement(null).print(writer);
      }
    }
    writer.append(ctx, '\n');

  }


  public List<String> getValues() {
    return values;
  }
  
}
