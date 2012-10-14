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

import org.crsh.text.Chunk;
import org.crsh.text.Renderer;
import org.crsh.text.Style;
import org.crsh.text.Text;
import org.crsh.util.Utils;

import java.util.Iterator;

public class TextElement extends Element {

  /** . */
  final Iterable<Chunk> stream;

  /** . */
  final int minWidth;

  /** . */
  final int width;

  private static int width(int width, Iterator<Chunk> stream, Text current, Integer from) {
    while (current == null) {
      if (stream.hasNext()) {
        Chunk next = stream.next();
        if (next instanceof Text) {
          current = (Text)next;
          from = 0;
        }
      } else {
        break;
      }
    }
    if (current == null) {
      return width;
    } else {
      int pos = Utils.indexOf(current.getText(), from, '\n');
      if (pos == -1) {
        return width(width + current.getText().length() - from, stream, current, from);
      } else {
        return Math.max(width + pos - from, width(0, stream, null, 0));
      }
    }
  }

  public TextElement(Iterable<Chunk> stream, int minWidth) {
    if (minWidth < 0) {
      throw new IllegalArgumentException("No negative min size allowed");
    }

    // Determine width
    int width = width(0, stream.iterator(), null, null);

    //
    this.minWidth = Math.min(width, minWidth);
    this.stream = stream;
    this.width = width;
  }

  public TextElement(Iterable<Chunk> stream) {
    this(stream, 1);
  }

  public Renderer renderer() {
    // return new LabelRenderer(this);
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "TextElement[]";
  }

  @Override
  public TextElement style(Style.Composite style) {
    return (TextElement)super.style(style);
  }
}
