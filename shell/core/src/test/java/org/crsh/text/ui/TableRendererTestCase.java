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

import static org.crsh.text.ui.Element.label;
import static org.crsh.text.ui.Element.row;

public class TableRendererTestCase extends AbstractRendererTestCase {

  public void testSimple() throws Exception {

    TableElement table = new TableElement();
    table.withColumnLayout(Layout.rightToLeft());

    table.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    assertRender(table, 2,
        "ab",
        "cd");
  }

  public void testHeterogeneous() throws Exception {

    TableElement table = new TableElement(1, 2);

    table.
        add(row().
            add(label("foo"))
        ).
        add(row().
            add(label("aa")).
            add(label("bb"))
        ).
        add(row().
            add(label("cc")).
            add(label("dd")).
            add(label("ee"))
        );

    //
    assertRender(table, 3,
        "foo",
        "abb",
        "a  ",
        "cdd",
        "c  ");
  }

  public void testRenderWithoutBorder() throws Exception {
    TableElement table = new TableElement();
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));
    table.add(row);

    //
    table.withColumnLayout(Layout.rightToLeft());

    //
    assertRender(table, 11, "foobar     ");
    assertRender(table, 10, "foobar    ");
    assertRender(table, 9, "foobar   ");
    assertRender(table, 8, "foobar  ");
    assertRender(table, 7, "foobar ");
    assertRender(table, 6, "foobar");
    assertRender(table, 5, "fooba", "   r ");
    assertRender(table, 4, "foob", "   a", "   r");
    assertRender(table, 3, "foo");
    assertRender(table, 2, "fo", "o ");
    assertRender(table, 1, "f", "o", "o");
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 1));

    //
    assertRender(table, 11, "foo   bar  ");
    assertRender(table, 10, "foo  bar  ");
    assertRender(table, 9, "foo  bar ");
    assertRender(table, 8, "foo bar ");
    assertRender(table, 7, "foo bar");
    assertRender(table, 6, "foobar");
    assertRender(table, 5, "fooba", "   r ");
    assertRender(table, 4, "foba", "o r ");
    assertRender(table, 3, "fob", "o a", "  r");
    assertRender(table, 2, "fb", "oa", "or");
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 2));

    //
    assertRender(table, 11, "foo bar    ");
    assertRender(table, 10, "foobar    ");
    assertRender(table, 9, "foobar   ");
    assertRender(table, 8, "foobar  ");
    assertRender(table, 7, "fobar  ", "o      ");
    assertRender(table, 6, "fobar ", "o     ");
    assertRender(table, 5, "fobar", "o    ");
    assertRender(table, 4, "fbar", "o   ", "o   ");
    assertRender(table, 3, "fba", "or ", "o  ");
    assertRender(table, 2, "fo", "o ");
    assertRender(table, 1, "f", "o", "o");
    assertRender(table, 0);
  }

  public void testRenderWithBorder() throws Exception {
    TableElement table = new TableElement().border(Border.dashed);
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));
    table.add(row);

    //
    table.withColumnLayout(Layout.rightToLeft());

    //
    assertRender(table, 11, " -------   ", "|foo|bar|  ", " -------   ");
    assertRender(table, 10, " -------  ", "|foo|bar| ", " -------  ");
    assertRender(table, 9, " ------- ", "|foo|bar|", " ------- ");
    assertRender(table, 8, " ------ ", "|foo|ba|", "|   |r |", " ------ ");
    assertRender(table, 7, " ----- ", "|foo|b|", "|   |a|", "|   |r|"," ----- ");
    assertRender(table, 6, " ---  ", "|foo| ", " ---  ");
    assertRender(table, 5, " --- ", "|foo|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", "|o |", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|"," - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 1));

    //
    assertRender(table, 11, " --------- ", "|foo |bar |", " --------- ");
    assertRender(table, 10, " -------- ", "|foo |bar|", " -------- ");
    assertRender(table, 9, " ------- ", "|foo|bar|", " ------- ");
    assertRender(table, 8, " ------ ", "|foo|ba|", "|   |r |", " ------ ");
    assertRender(table, 7, " ----- ", "|fo|ba|", "|o |r |", " ----- ");
    assertRender(table, 6, " ---- ", "|fo|b|", "|o |a|", "|  |r|", " ---- ");
    assertRender(table, 5, " --- ", "|f|b|", "|o|a|", "|o|r|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", "|o |", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 2));

    //
    assertRender(table, 11, " --------- ", "|foo|bar  |", " --------- ");
    assertRender(table, 10, " -------- ", "|fo|bar  |", "|o |     |", " -------- ");
    assertRender(table, 9, " ------- ", "|fo|bar |", "|o |    |", " ------- ");
    assertRender(table, 8, " ------ ", "|fo|bar|", "|o |   |", " ------ ");
    assertRender(table, 7, " ----- ", "|f|bar|", "|o|   |", "|o|   |", " ----- ");
    assertRender(table, 6, " ---- ", "|f|ba|", "|o|r |", "|o|  |", " ---- ");
    assertRender(table, 5, " --- ", "|foo|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", "|o |", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);
  }

  public void testCosmetic() throws Exception {
    TableElement table = new TableElement();
    table.withColumnLayout(Layout.rightToLeft());
    RowElement row = new RowElement().add(new LabelElement("foo", 5), new LabelElement("This text is larger to be displayed in a cell of 32", 5));
    table.add(row);
    assertRender(table, 32,
        "fooThis text is larger to be dis",
        "   played in a cell of 32       ");
  }

  public void testCosmeticWithBorder() throws Exception {
    TableElement table = new TableElement();
    table.withColumnLayout(Layout.rightToLeft());
    RowElement row = new RowElement().add(new LabelElement("foo", 5), new LabelElement("This text is larger to be displayed in a cell of 32", 5));
    table.border(Border.dashed);
    table.add(row);
    assertRender(table, 32,
        " ------------------------------ ",
        "|foo|This text is larger to be |",
        "|   |displayed in a cell of 32 |",
        " ------------------------------ ");
  }

  public void testBorderStyle() throws Exception {

    TableElement table = new TableElement();
    table.withColumnLayout(Layout.rightToLeft());
    table.border(Border.dashed);

    table.
        add(row().style(Color.blue.fg().bg(Color.green).bold()).
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c").style(Color.blue.fg().bg(Color.green).bold())).
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
}
