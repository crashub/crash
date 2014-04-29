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

import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;

import java.util.Iterator;
import java.util.LinkedList;

public class ElementRenderer extends Renderer<Element> {

  @Override
  public Class<Element> getType() {
    return Element.class;
  }

  @Override
  public LineRenderer renderer(Iterator<Element> stream) {
    if (stream.hasNext()) {
      Element element = stream.next();
      if (stream.hasNext()) {
        LinkedList<LineRenderer> renderers = new LinkedList<LineRenderer>();
        renderers.add(element.renderer());
        while (stream.hasNext()) {
          element = stream.next();
          renderers.add(element.renderer());
        }
        return LineRenderer.vertical(renderers);
      } else {
        return element.renderer();
      }
    } else {
      throw new UnsupportedOperationException("todo");
    }
  }
}
