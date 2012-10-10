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
import org.crsh.text.ChunkBuffer;
import org.crsh.text.ui.Element;
import org.crsh.text.ui.Renderer;
import org.crsh.text.ui.RendererAppendable;
import org.crsh.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractRendererTestCase extends AbstractTestCase {

  public List<String> render(Renderer renderer) {
    ArrayList<String> result = new ArrayList<String>();
    while (renderer.hasLine()) {
      ChunkBuffer buffer = new ChunkBuffer();
      renderer.renderLine(new RendererAppendable(buffer));
      StringBuilder sb = new StringBuilder();
      try {
        buffer.writeAnsiTo(sb);
      }
      catch (IOException e) {
        throw failure(e);
      }
      result.add(sb.toString());
    }
    return result;
  }

  public List<String> render(Element element, int width) {
    Renderer renderer = element.renderer(width);
    return render(renderer);
  }

  public void assertRender(Element element, int width, String... expected) {
    Renderer renderer = element.renderer(width);
    assertRender(renderer, expected);
  }

  public void assertRender(Renderer renderer, String... expected) {
    List<String> result = render(renderer);
    if (result.size() != expected.length) {
      throw failure("Was expecting the same number of lines got:" + Strings.join(result, "/") + " expected:" +
          Strings.join(Arrays.asList(expected), "/"));
    } else {
      for (int i = 0;i < expected.length;i++) {
        if (!result.get(i).equals(expected[i])) {
          throw failure("Was expecting line " + i + " <" + result.get(i) + "> to be equals to <" + expected[i] + ">");
        }
      }
    }
  }
}
