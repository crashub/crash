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

package org.crsh.text;

import org.crsh.shell.ScreenContext;

import java.io.IOException;
import java.util.LinkedList;

public class ChunkAdapter implements ScreenContext {

  /** . */
  private final LinkedList<Object> buffer = new LinkedList<Object>();

  /** . */
  private Renderable renderable = null;

  /** . */
  private final RenderAppendable out;

  public ChunkAdapter(ScreenContext out) {
    this.out = new RenderAppendable(out);
  }

  public int getWidth() {
    return out.getWidth();
  }

  public int getHeight() {
    return out.getHeight();
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void write(Chunk chunk) throws IOException {
    provide(chunk);
  }

  public void provide(Object element) throws IOException {
    Renderable current = Renderable.getRenderable(element.getClass());
    if (current == null) {
      send();
      if (element instanceof Chunk) {
        out.write((Chunk)element);
      } else {
        out.write(Text.create(element.toString()));
      }
    } else {
      if (renderable != null && !current.equals(renderable)) {
        send();
      }
      buffer.addLast(element);
      renderable = current;
    }
  }

  public void flush() throws IOException {
    send();
    out.flush();
  }

  public void send() throws IOException {
    if (buffer.size() > 0) {
      Renderer renderer = renderable.renderer(buffer.iterator());
      renderer.render(out);
      buffer.clear();
      renderable = null;
    }
  }
}
