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
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.util.BaseIterator;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * A renderer for {@link java.util.logging.LogRecord} objects based on the {@link java.util.logging.SimpleFormatter}
 * formatter.
 *
 * @author Julien Viet
 */
public class LogRecordRenderer extends Renderer<LogRecord> {

  @Override
  public Class<LogRecord> getType() {
    return LogRecord.class;
  }

  @Override
  public LineRenderer renderer(final Iterator<LogRecord> stream) {
    return LineRenderer.vertical(new Iterable<LineRenderer>() {
      @Override
      public Iterator<LineRenderer> iterator() {
        return new BaseIterator<LineRenderer>() {
          final SimpleFormatter formatter = new SimpleFormatter();
          @Override
          public boolean hasNext() {
            return stream.hasNext();
          }
          @Override
          public LineRenderer next() {
            LogRecord record = stream.next();
            String line = formatter.format(record);
            Color color;
            if (record.getLevel() == Level.SEVERE) {
              color = Color.red;
            } else if (record.getLevel() == Level.WARNING) {
              color = Color.yellow;
            } else if (record.getLevel() == Level.INFO) {
              color = Color.green;
            } else {
              color = Color.blue;
            }
            return new LabelElement(line).style(color.fg()).renderer();
          }
        };
      }
    });
  }
}
