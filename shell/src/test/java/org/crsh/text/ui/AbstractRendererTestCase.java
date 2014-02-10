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

import org.crsh.AbstractTestCase;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkBuffer;
import org.crsh.text.Format;
import org.crsh.text.LineReader;
import org.crsh.text.RenderAppendable;
import org.crsh.shell.ScreenContext;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractRendererTestCase extends AbstractTestCase {

  public List<String> render(final LineReader renderer, final int width) {
    ArrayList<String> result = new ArrayList<String>();
    while (renderer.hasLine()) {
      final ChunkBuffer buffer = new ChunkBuffer();
      renderer.renderLine(new RenderAppendable(new ScreenContext() {
        public int getWidth() {
          return width;
        }
        public int getHeight() {
          return 40;
        }
        public Class<Chunk> getConsumedType() {
          return Chunk.class;
        }
        public void write(Chunk chunk) throws IOException {
          provide(chunk);
        }
        public void provide(Chunk element) throws IOException {
          buffer.provide(element);
        }
        public void flush() throws IOException {
          buffer.flush();
        }
      }));
      StringBuilder sb = new StringBuilder();
      try {
        buffer.format(Format.ANSI, sb);
      }
      catch (IOException e) {
        throw failure(e);
      }
      result.add(sb.toString());
    }
    return result;
  }

  public List<String> render(Element element, int width) {
    LineReader renderer = element.renderer().reader(width);
    return render(renderer, width);
  }

  public void assertRender(Element element, int width, int height, String... expected) {
    LineReader renderer = element.renderer().reader(width, height);
    assertNotNull("Was expecting a renderer", renderer);
    assertRender(renderer, width, expected);
  }

  public void assertRender(Element element, int width, String... expected) {
    LineReader renderer = element.renderer().reader(width);
    assertNotNull("Was expecting a renderer", renderer);
    assertRender(renderer, width, expected);
  }

  public void assertNoRender(Element element, int width) {
    LineReader renderer = element.renderer().reader(width);
    assertNull(renderer);
  }

  public void assertNoRender(Element element, int width, int height) {
    LineReader renderer = element.renderer().reader(width, height);
    assertNull(renderer);
  }

  public void assertRender(LineReader renderer, int width, String... expected) {
    List<String> result = render(renderer, width);
    if (result.size() != expected.length) {
      throw failure("Was expecting the same number of lines got:" + Utils.join(result, "/") + " expected:" +
          Utils.join(Arrays.asList(expected), "/"));
    } else {
      for (int i = 0;i < expected.length;i++) {
        if (!result.get(i).equals(expected[i])) {
          throw failure("Was expecting line " + i + " <" + result.get(i) + "> to be equals to <" + expected[i] + ">");
        }
      }
    }
  }
}
