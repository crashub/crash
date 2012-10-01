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

import java.io.IOException;

public class LabelRendererTestCase extends AbstractRendererTestCase {

  public void testRender() throws IOException {
    assertRender(new LabelElement(""), 5, "     ");

    // Not sure this one is good
    assertRender(new LabelElement("\n"), 5, "     ");

    //
    assertRender(new LabelElement("a"), 5, "a    ");
    assertRender(new LabelElement("a"), 4, "a   ");
    assertRender(new LabelElement("a"), 3, "a  ");
    assertRender(new LabelElement("a"), 2, "a ");
    assertRender(new LabelElement("a"), 1, "a");
    assertRender(new LabelElement("a"), 0, "");

    //
    assertRender(new LabelElement("ab"), 1, "a", "b");
    assertRender(new LabelElement("bar"), 2, "ba", "r ");
    assertRender(new LabelElement("a\nb"), 2, "a ", "b ");
    assertRender(new LabelElement("a\nb"), 1, "a", "b");

    LabelElement label = new LabelElement("NAME");
//    Renderer r = label.renderer(1);
//    assertTrue(r.hasLine());
//    r.renderLine(new ChunkBuffer());
//    assertTrue(r.hasLine());
//    r.renderLine(new ChunkBuffer());
//    assertTrue(r.hasLine());
//    r.renderLine(new ChunkBuffer());
//    assertTrue(r.hasLine());
//    r.renderLine(new ChunkBuffer());
//    assertFa(r.hasLine());

  }
}
