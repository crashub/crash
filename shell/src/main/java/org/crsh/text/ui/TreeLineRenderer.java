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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class TreeLineRenderer extends LineRenderer {

  /** . */
  private final LineRenderer value;

  /** . */
  private final List<LineRenderer> children;

  TreeLineRenderer(TreeElement tree) {

    ArrayList<LineRenderer> children = new ArrayList<LineRenderer>(tree.children.size());
    for (Element child : tree.children) {
      children.add(child.renderer());
    }

    //
    this.children = children;
    this.value = tree.value != null ? tree.value.renderer() : null;
  }

  @Override
  public int getActualWidth() {
    int width = value != null ? value.getActualWidth() : 0;
    for (LineRenderer child : children) {
      width = Math.max(width, 2 + child.getActualWidth());
    }
    return width;
  }

  @Override
  public int getMinWidth() {
    int width = value != null ? value.getMinWidth() : 0;
    for (LineRenderer child : children) {
      width = Math.max(width, 2 + child.getMinWidth());
    }
    return width;
  }

  @Override
  public int getActualHeight(int width) {
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  public int getMinHeight(int width) {
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  public LineReader reader(final int width) {


    final LinkedList<LineReader> readers  = new LinkedList<LineReader>();
    for (LineRenderer child : children) {
      readers.addLast(child.reader(width - 2));
    }

    //
    return new LineReader() {

      /** . */
      LineReader value = TreeLineRenderer.this.value != null ? TreeLineRenderer.this.value.reader(width) : null;

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
        while (readers.size() > 0) {
          if (readers.peekFirst().hasLine()) {
            return true;
          } else {
            readers.removeFirst();
            node = true;
          }
        }
        return false;
      }

      public void renderLine(RenderAppendable to) {
        if (value != null) {
          if (value.hasLine()) {
            value.renderLine(to);
          } else {
            value = null;
          }
        }
        if (value == null) {
          while (readers.size() > 0) {
            LineReader first = readers.peekFirst();
            if (first.hasLine()) {
              if (node) {
                to.append("+-");
                node = false;
              } else {
                Iterator<LineReader> i = readers.descendingIterator();
                boolean rest = false;
                while (i.hasNext()) {
                  LineReader renderer = i.next();
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
}
