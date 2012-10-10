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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeElement extends Element {

  /** An optional value element. */
  private Element value;

  /** . */
  private List<Element> children = new ArrayList<Element>();

  public TreeElement() {
    this(null);
  }

  public TreeElement(Element value) {
    this.value = value;
  }

  public TreeElement addChild(Element child) {
    if (child.parent != null) {
      throw new IllegalArgumentException("Child has already a parent");
    }
    children.add(child);
    child.parent =  this;
    return this;
  }

  public int getSize() {
    return children.size();
  }

  public Element getValue() {
    return value;
  }

  public Element getNode(int index) {
    return children.get(index);
  }


  @Override
  public Renderer renderer(final int width) {


    final LinkedList<Renderer> renderers  = new LinkedList<Renderer>();
    for (Element child : children) {
      renderers.addLast(child.renderer(width - 2));
    }


    return new Renderer() {

      /** . */
      Renderer value = TreeElement.this.value != null ? TreeElement.this.value.renderer(width) : null;

      /** . */
      boolean node = true;

      public boolean hasLine() {
        if (value != null) {
          if (value.hasLine()) {
            return true;
          } else {
            value = null;
          }
        }
        while (renderers.size() > 0) {
          if (renderers.peekFirst().hasLine()) {
            return true;
          } else {
            renderers.removeFirst();
            node = true;
          }
        }
        return false;
      }

      public void renderLine(RendererAppendable to) {
        if (value != null) {
          if (value.hasLine()) {
            value.renderLine(to);
          } else {
            value = null;
          }
        }
        if (value == null) {
          while (renderers.size() > 0) {
            Renderer first = renderers.peekFirst();
            if (first.hasLine()) {
              if (node) {
                to.append("+-");
                node = false;
              } else {
                Iterator<Renderer> i = renderers.descendingIterator();
                boolean rest = false;
                while (i.hasNext()) {
                  Renderer renderer = i.next();
                  if (i.hasNext()) {
                    if (renderer.hasLine()) {
                      rest = true;
                      break;
                    }
                  }
                }
                if (rest) {
                  to.append("| ");
                } else {
                  to.append("  ");
                }
              }
              first.renderLine(to);
              break;
            }
          }
        }
      }
    };
  }

  @Override
  int getWidth() {
    return 0;
  }
}
