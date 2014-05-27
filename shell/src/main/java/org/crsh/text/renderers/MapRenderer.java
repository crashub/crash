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

package org.crsh.text.renderers;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

public class MapRenderer extends Renderer<Map<?, ?>> {

  @Override
  public Class<Map<?, ?>> getType() {
    Object mapClass = Map.class;
    return (Class<Map<?,?>>)mapClass;
  }

  @Override
  public LineRenderer renderer(Iterator<Map<?, ?>> stream) {

    TableElement table = new TableElement();
    LinkedHashSet<String> current = new LinkedHashSet<String>();
    LinkedHashSet<String> bilto = new LinkedHashSet<String>();

    ArrayList<LineRenderer> renderers = new ArrayList<LineRenderer>();

    while (stream.hasNext()) {

      Map<?, ?> row = stream.next();

      if (row.size() >  0) {

        bilto.clear();
        for (Map.Entry<?, ?> entry : row.entrySet()) {
          bilto.add(String.valueOf(entry.getKey()));
        }

        // Create a new table if needed
        if (!current.equals(bilto)) {
          if (table.getRows().size() > 0) {
            renderers.add(table.renderer());
          }
          table = new TableElement().rightCellPadding(1);
          RowElement header = new RowElement(true);
          header.style(Decoration.bold.fg(Color.black).bg(Color.white));
          for (String s : bilto) {
            header.add(s);
          }
          table.add(header);
          current = bilto;
        }

        //
        RowElement r = new RowElement();
        for (String s : bilto) {
          r.add(String.valueOf(row.get(s)));
        }
        table.add(r);
      }
    }

    //
    if (table.getRows().size() > 0) {
      renderers.add(table.renderer());
    }

    //
    return LineRenderer.vertical(renderers);
  }
}
