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
import org.crsh.text.LineReader;
import org.crsh.text.RenderAppendable;
import org.crsh.text.Renderer;

import java.util.Iterator;

public class TextRenderer extends Renderer {

  /** . */
  final TextElement text;

  public TextRenderer(TextElement text) {
    this.text = text;
  }

  @Override
  public int getActualWidth() {
    return text.width;
  }

  @Override
  public int getMinWidth() {
    return text.minWidth;
  }

  @Override
  public int getActualHeight(int width) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMinHeight(int width) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LineReader reader(final int width) {
    return new LineReader() {

      /** . */
      Iterator<Chunk> iterator = text.stream.iterator();

      /** . */
      Chunk current;

      public boolean hasLine() {
        return false;
      }

      public void renderLine(RenderAppendable to) throws IllegalStateException {
        throw new UnsupportedOperationException("To finish");
      }
    };
  }
}
