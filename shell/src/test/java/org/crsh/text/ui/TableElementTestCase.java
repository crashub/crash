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

import org.crsh.text.Color;

import static org.crsh.text.ui.Element.*;

public class TableElementTestCase extends AbstractRendererTestCase {

  public void testSimple() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));


    assertRender(tableElement, 12,
        "ab          ",
        "cd          ");
  }

  public void testStyle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().style(Color.green.bg().fg(Color.blue).bold()).
            add(label("a")).
            add(label("b")))
      .add(row().
          add(label("c").style(Color.green.bg().fg(Color.blue).bold())).
          add(label("d")));

/*
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    String expected = "\u001B[1;34;42ma     b     \u001B[0m_\u001B[1;34;42mc     \u001B[0md     \u001B[0m_";

    StringBuilder sb = new StringBuilder();
    reader.writeAnsiTo(sb);
    String ansi = sb.toString();

    //
    assertEquals(
      expected
      , ansi);
*/

  }

  public void testInNode() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 14,
        "+-foo         ",
        "+-ab          ",
        "| cd          ",
        "+-bar         ");

  }

  public void testInNodeBorder() throws Exception {

    TableElement tableElement = new TableElement(1, 1);
    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 32,
        "+-foo                           ",
        "+- ---------------------------- ",
        "| |a             b             |",
        "| |c             d             |",
        "|  ---------------------------- ",
        "+-bar                           ");
  }

  public void testInNodeTooLarge() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("b")).
            add(label("c is a very very very too long value"))).
        add(row().
            add(label("d")).
            add(label("e")).
            add(label("f")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 24,
        "+-foo                   " ,
        "+-abc is a very very ver" ,
        "|   y too long value    " ,
        "| def                   " ,
        "+-bar                   ");
  }

  public void testInNodeTooLargeBorder() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.border(BorderStyle.DASHED);
    tableElement.separator(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b")).
            add(label("c is a very very very too long value"))).
        add(row().
            add(label("d")).
            add(label("e")).
            add(label("f")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 32,
        "+-foo                           ",
        "+- ---------------------------- ",
        "| |a|b|c is a very very very to|",
        "| | | |o long value            |",
        "| |d|e|f                       |",
        "|  ---------------------------- ",
        "+-bar                           ");
  }

  public void testInNodeHeader() throws Exception {

    TableElement tableElement = new TableElement().border(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(header().
            add(label("e")).
            add(label("f"))).
        add(row().
            add(label("g")).
            add(label("h"))).
        add(row().
            add(label("i")).
            add(label("j")));

    TreeElement node = new TreeElement();
    node.addChild(label("foo"));
    node.addChild(tableElement);
    node.addChild(label("bar"));

    assertRender(node, 32,
        "+-foo                           ",
        "+- --                           ",
        "| |ab|                          ",
        "| |cd|                          ",
        "|  --                           ",
        "| |ef|                          ",
        "|  --                           ",
        "| |gh|                          ",
        "| |ij|                          ",
        "|  --                           ",
        "+-bar                           ");
  }

  public void testTooLarge() throws Exception {
    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32"))).
        add(row().
            add(label("c")).
            add(label("d")));

    assertRender(tableElement, 27,
        "aThis text is very ver very",
        "  too large to be displayed",
        "  in a cell of 32          ",
        "cd                         "
    );
  }

  public void testSimpleBorder() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    //
    tableElement.border(BorderStyle.DASHED);
    assertRender(tableElement, 32,
        " --                             ",
        "|ab|                            ",
        "|cd|                            ",
        " --                             ");

    //
    tableElement.border(BorderStyle.STAR);
    tableElement.separator(BorderStyle.STAR);
    assertRender(tableElement, 32,
        "*****                           ",
        "*a*b*                           ",
        "*c*d*                           ",
        "*****                           ");
  }

  public void testBorderHeaderTopBottom() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(header().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(row().
            add(label("e")).
            add(label("f"))).
        add(header().
            add(label("g")).
            add(label("h")));

    assertRender(tableElement, 32,
        " --                             ",
        "|ab|                            ",
        " --                             ",
        "|cd|                            ",
        "|ef|                            ",
        " --                             ",
        "|gh|                            ",
        " --                             ");
  }

  public void testNoBorderHeaderTopBottom() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.border(null);

    tableElement.
        add(header().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(row().
            add(label("e")).
            add(label("f")));

    assertRender(tableElement, 3,
        "ab ",
        "-- ",
        "cd ",
        "ef ");

  }

  public void testBorderHeaderMiddle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(header().
            add(label("e")).
            add(label("f"))).
        add(row().
            add(label("g")).
            add(label("h"))).
        add(row().
            add(label("i")).
            add(label("j")));

    assertRender(tableElement, 32,
        " --                             ",
        "|ab|                            ",
        "|cd|                            ",
        " --                             ",
        "|ef|                            ",
        " --                             ",
        "|gh|                            ",
        "|ij|                            ",
        " --                             ");
  }

  public void testBorderHeaderTwoMiddle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(header().
            add(label("e")).
            add(label("f"))).
        add(header().
            add(label("g")).
            add(label("h"))).
        add(row().
            add(label("i")).
            add(label("j"))).
        add(row().
            add(label("k")).
            add(label("l")));

    assertRender(tableElement, 32,
        " --                             ",
        "|ab|                            ",
        "|cd|                            ",
        " --                             ",
        "|ef|                            ",
        " --                             ",
        "|gh|                            ",
        " --                             ",
        "|ij|                            ",
        "|kl|                            ",
        " --                             ");

  }

  public void testTooLargeBorder() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.border(BorderStyle.DASHED);
    tableElement.separator(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32"))).
        add(row().
            add(label("c")).
            add(label("d")));

    assertRender(tableElement, 32,
        " ------------------------------ ",
        "|a|This text is very ver very t|",
        "| |oo large to be displayed in |",
        "| |a cell of 32                |",
        "|c|d                           |",
        " ------------------------------ ");
  }

  public void testTooLargeBorderHeader() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.border(BorderStyle.DASHED);
    tableElement.separator(BorderStyle.DASHED);

    tableElement.
        add(header().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32"))).
        add(row().
            add(label("c")).
            add(label("d")));

    assertRender(tableElement, 32,
        " ------------------------------ ",
        "|a|This text is very ver very t|",
        "| |oo large to be displayed in |",
        "| |a cell of 32                |",
        " ------------------------------ ",
        "|c|d                           |",
        " ------------------------------ ");
  }

  public void testBorderStyle() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(row().style(Color.green.bg().fg(Color.blue).bold()).
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c").style(Color.green.bg().fg(Color.blue).bold())).
            add(label("d")));

/*
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    String expected =
      " --------------- _|" +
      "\u001B[1;34;42m a     \u001B[0m|\u001B[1;34;42m b     \u001B[0m|_" +
      "|\u001B[1;34;42m c     \u001B[0m| \u001B[0md     \u001B[0m|_" +
      " --------------- _";

    StringBuilder sb = new StringBuilder();
    reader.writeAnsiTo(sb);
    String ansi = sb.toString();

    //
    assertEquals(
      expected
      , ansi);
*/

  }

  public void testTooManyColumns() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.separator(BorderStyle.DASHED);
    tableElement.border(BorderStyle.DASHED);

    tableElement.
        add(row().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32")).
            add(label("b")));

    assertRender(tableElement, 32,
        " ------------------------------ ",
        "|a|This text is very ver very t|",
        "| |oo large to be displayed in |",
        "| |a cell of 32                |",
        " ------------------------------ ");
  }
}
