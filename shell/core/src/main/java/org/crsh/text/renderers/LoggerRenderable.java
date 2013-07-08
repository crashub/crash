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
import org.crsh.text.Renderable;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerRenderable extends Renderable<Logger> {

  @Override
  public Class<Logger> getType() {
    return Logger.class;
  }

  @Override
  public Renderer renderer(Iterator<Logger> stream) {
    TableElement table = new TableElement();

    // Header
    RowElement header = new RowElement();
    header.style(Decoration.bold.fg(Color.black).bg(Color.white));
    header.add(new LabelElement("NAME"));
    header.add(new LabelElement("LEVEL"));
    table.add(header);

    //
    while (stream.hasNext()) {
      Logger logger = stream.next();

      // Determine level
      String level;
      if (logger.isLoggable(Level.FINER)) {
        level = "TRACE";
      } else if (logger.isLoggable(Level.FINE)) {
        level = "DEBUG";
      } else if (logger.isLoggable(Level.INFO)) {
        level = "INFO";
      } else if (logger.isLoggable(Level.WARNING)) {
        level = "WARN";
      }  else if (logger.isLoggable(Level.SEVERE)) {
        level = "ERROR";
      } else {
        level = "UNKNOWN";
      }

      //
      RowElement row = new RowElement();
      row.add(new LabelElement(logger.getName()));
      row.add(new LabelElement(level));
      table.add(row);
    }

    //
    return table.renderer();
  }
}
