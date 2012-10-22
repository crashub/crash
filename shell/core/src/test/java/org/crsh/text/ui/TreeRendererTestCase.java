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

public class TreeRendererTestCase extends AbstractRendererTestCase {

  public void testSimple() throws IOException {
    TreeElement elt = new TreeElement();
    elt.addChild(new LabelElement("1\n1"));
    elt.addChild(new LabelElement("2"));

    assertRender(elt, 4,
        "+-1 ",
        "| 1 ",
        "+-2 ");
  }

/*
  public void testFoo() throws Exception {
    TreeElement elt = new TreeElement();
    elt.addChild(new LabelElement("1\n1"));
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");
    elt.print(writer, new TestInvocationContext());
    assertEquals(
        "+-1_" +
            "  1_"
        , reader.toString());
  }
  */

  public void testNested() throws Exception {
    TreeElement elt = new TreeElement(new LabelElement("foo"));
    elt.addChild(new TreeElement(new LabelElement("bar")).addChild(new LabelElement("1\n1")).addChild(new LabelElement("2\n2")));
    elt.addChild(new TreeElement().addChild(new LabelElement("3")).addChild(new LabelElement("4")));

    assertRender(elt, 6,
        "foo   ",
        "+-bar ",
        "| +-1 ",
        "| | 1 ",
        "| +-2 ",
        "|   2 ",
        "+-+-3 ",
        "  +-4 ");
  }

  public void testTooLarge() throws IOException {
    TreeElement elt = new TreeElement();
    elt.addChild(new LabelElement("foo value is very very very too large for the console"));
    elt.addChild(new LabelElement("bar\n"));

    assertRender(elt, 32,
        "+-foo value is very very very to",
        "| o large for the console       ",
        "+-bar                           ",
        "                                ");
  }
}
