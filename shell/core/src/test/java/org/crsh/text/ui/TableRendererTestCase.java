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
    table.withColumnLayout(Layout.flow());

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
    assertEquals(6, table.renderer().getActualWidth());
    assertEquals(2, table.renderer().getMinWidth());

    //
    table.withColumnLayout(Layout.flow());

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
    TableElement table = new TableElement().border(BorderStyle.DASHED);
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));
    table.add(row);

    //
    assertEquals(8, table.renderer().getActualWidth());
    assertEquals(4, table.renderer().getMinWidth());

    //
    table.withColumnLayout(Layout.flow());

    //
    assertRender(table, 11, " ------    ", "|foobar|   ", " ------    ");
    assertRender(table, 10, " ------   ", "|foobar|  ", " ------   ");
    assertRender(table, 9, " ------  ", "|foobar| ", " ------  ");
    assertRender(table, 8, " ------ ", "|foobar|", " ------ ");
    assertRender(table, 7, " ----- ", "|fooba|", "|   r |", " ----- ");
    assertRender(table, 6, " ---- ", "|foob|", "|   a|", "|   r|", " ---- ");
    assertRender(table, 5, " --- ", "|foo|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", "|o |", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|"," - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 1));

    //
    assertRender(table, 11, " --------- ", "|foo  bar |", " --------- ");
    assertRender(table, 10, " -------- ", "|foo bar |", " -------- ");
    assertRender(table, 9, " ------- ", "|foo bar|", " ------- ");
    assertRender(table, 8, " ------ ", "|foobar|", " ------ ");
    assertRender(table, 7, " ----- ", "|fooba|", "|   r |", " ----- ");
    assertRender(table, 6, " ---- ", "|foba|", "|o r |", " ---- ");
    assertRender(table, 5, " --- ", "|fob|", "|o a|", "|  r|", " --- ");
    assertRender(table, 4, " -- ", "|fb|", "|oa|", "|or|", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 2));

    //
    assertRender(table, 11, " --------- ", "|foobar   |", " --------- ");
    assertRender(table, 10, " -------- ", "|foobar  |", " -------- ");
    assertRender(table, 9, " ------- ", "|fobar  |", "|o      |", " ------- ");
    assertRender(table, 8, " ------ ", "|fobar |", "|o     |", " ------ ");
    assertRender(table, 7, " ----- ", "|fobar|", "|o    |", " ----- ");
    assertRender(table, 6, " ---- ", "|fbar|", "|o   |", "|o   |", " ---- ");
    assertRender(table, 5, " --- ", "|fba|", "|or |", "|o  |", " --- ");
    assertRender(table, 4, " -- ", "|fo|", "|o |", " -- ");
    assertRender(table, 3, " - ", "|f|", "|o|", "|o|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);
  }

  public void testRenderWithOverflowHidden() throws Exception {
    TableElement table = new TableElement().border(BorderStyle.DASHED).overflow(Overflow.HIDDEN);
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));
    table.add(row);

    //
    table.withColumnLayout(Layout.flow());

    //
    assertRender(table, 11, " ------    ", "|foobar|   ", " ------    ");
    assertRender(table, 10, " ------   ", "|foobar|  ", " ------   ");
    assertRender(table, 9, " ------  ", "|foobar| ", " ------  ");
    assertRender(table, 8, " ------ ", "|foobar|", " ------ ");
    assertRender(table, 7, " ----- ", "|fooba|", " ----- ");
    assertRender(table, 6, " ---- ", "|foob|", " ---- ");
    assertRender(table, 5, " --- ", "|foo|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", " -- ");
    assertRender(table, 3, " - ", "|f|"," - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 1));

    //
    assertRender(table, 11, " --------- ", "|foo  bar |", " --------- ");
    assertRender(table, 10, " -------- ", "|foo bar |", " -------- ");
    assertRender(table, 9, " ------- ", "|foo bar|", " ------- ");
    assertRender(table, 8, " ------ ", "|foobar|", " ------ ");
    assertRender(table, 7, " ----- ", "|fooba|", " ----- ");
    assertRender(table, 6, " ---- ", "|foba|", " ---- ");
    assertRender(table, 5, " --- ", "|fob|", " --- ");
    assertRender(table, 4, " -- ", "|fb|", " -- ");
    assertRender(table, 3, " - ", "|f|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);

    //
    table.withColumnLayout(Layout.weighted(1, 2));

    //
    assertRender(table, 11, " --------- ", "|foobar   |", " --------- ");
    assertRender(table, 10, " -------- ", "|foobar  |", " -------- ");
    assertRender(table, 9, " ------- ", "|fobar  |", " ------- ");
    assertRender(table, 8, " ------ ", "|fobar |", " ------ ");
    assertRender(table, 7, " ----- ", "|fobar|", " ----- ");
    assertRender(table, 6, " ---- ", "|fbar|", " ---- ");
    assertRender(table, 5, " --- ", "|fba|", " --- ");
    assertRender(table, 4, " -- ", "|fo|", " -- ");
    assertRender(table, 3, " - ", "|f|", " - ");
    assertRender(table, 2);
    assertRender(table, 1);
    assertRender(table, 0);
  }

  public void testRenderWithBorderAndSeparator() throws Exception {
    TableElement table = new TableElement().border(BorderStyle.DASHED).separator(BorderStyle.DASHED);
    RowElement row = new RowElement().add(new LabelElement("foo"), new LabelElement("bar"));
    table.add(row);

    //
    assertEquals(9, table.renderer().getActualWidth());
    assertEquals(5, table.renderer().getMinWidth());

    //
    table.withColumnLayout(Layout.flow());

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
    table.withColumnLayout(Layout.flow());
    RowElement row = new RowElement().add(new LabelElement("foo", 5), new LabelElement("This text is larger to be displayed in a cell of 32", 5));
    table.add(row);
    assertRender(table, 32,
        "fooThis text is larger to be dis",
        "   played in a cell of 32       ");
  }

  public void testCosmeticWithBorder() throws Exception {
    TableElement table = new TableElement();
    table.withColumnLayout(Layout.flow());
    RowElement row = new RowElement().add(new LabelElement("foo", 5), new LabelElement("This text is larger to be displayed in a cell of 32", 5));
    table.separator(BorderStyle.DASHED);
    table.border(BorderStyle.DASHED);
    table.add(row);
    assertRender(table, 32,
        " ------------------------------ ",
        "|foo|This text is larger to be |",
        "|   |displayed in a cell of 32 |",
        " ------------------------------ ");
  }

  public void testBorderStyle() throws Exception {

    TableElement table = new TableElement();
    table.withColumnLayout(Layout.flow());
    table.border(BorderStyle.DASHED);

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

  public void testCellPadding() {
    TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(2);
    table.add(new RowElement().add(new LabelElement("foo"), new LabelElement("bar")));

    //
    assertEquals(12, table.renderer().getActualWidth());
    assertEquals(8, table.renderer().getMinWidth());

    //
    assertRender(table, 12, " foo   bar  ");
    assertRender(table, 11, " foo   ba  ", "       r   ");
    assertRender(table, 10, " foo   b  ", "       a  ", "       r  ");
    assertRender(table, 9, " foo     ");
    assertRender(table, 8, " foo    ");
    assertRender(table, 7, " foo   ");
    assertRender(table, 6, " foo  ");
    assertRender(table, 5, " fo  ", " o   ");
    assertRender(table, 4, " f  ", " o  ", " o  ");

    // IT SHOULD BE NO RENDER
    assertRender(table, 3);
  }

  public void testRowLayout() {
    TableElement table = new TableElement();
    table.add(new RowElement().add(new LabelElement("foo")));
    table.add(new RowElement().add(new LabelElement("bar")));

    //
    assertRender(table, 3, 2, "foo", "bar");
    assertRender(table, 3, 1, "foo");
    assertRender(table, 2, 4, "fo", "o ", "ba", "r ");
    assertRender(table, 2, 3, "fo", "o ", "  ");
  }

  public void testRowLayoutWithHeader() {
    TableElement table = new TableElement();
    table.add(new RowElement(true).add(new LabelElement("foo")));
    table.add(new RowElement().add(new LabelElement("bar")));

    //
    assertRender(table, 3, 3, "foo", "---", "bar");
    assertRender(table, 3, 2, "foo", "   ");
    assertNoRender(table, 3, 1);

    //
    assertRender(table, 2, 5, "fo", "o ", "--", "ba", "r ");
    assertRender(table, 2, 4, "fo", "o ", "  ", "  ");
    assertRender(table, 2, 3, "fo", "o ", "  ");
  }

  public void testRowLayoutWithBorder() {
    TableElement table = new TableElement().border(BorderStyle.DASHED);
    table.add(new RowElement().add(new LabelElement("foo")));
    table.add(new RowElement().add(new LabelElement("bar")));

    //
    assertRender(table, 5, 4, " --- ", "|foo|", "|bar|", " --- ");
    assertRender(table, 5, 3, " --- ", "|foo|", " --- ");
    assertNoRender(table, 5, 2);

    //
    assertRender(table, 4, 6, " -- ", "|fo|", "|o |", "|ba|", "|r |", " -- ");
    assertRender(table, 4, 5, " -- ", "|fo|", "|o |", "|  |", " -- ");
    assertRender(table, 4, 4, " -- ", "|fo|", "|o |", " -- ");
  }

  public void testRowLayoutWithHeaderBorder() {
    TableElement table = new TableElement().border(BorderStyle.DASHED);
    table.add(new RowElement(true).add(new LabelElement("foo")));
    table.add(new RowElement().add(new LabelElement("bar")));

    //
    assertRender(table, 5, 5, " --- ", "|foo|", " --- ", "|bar|", " --- ");
    assertRender(table, 5, 4, " --- ", "|foo|", "|   |", " --- ");
    assertNoRender(table, 5, 3);

    //
    assertRender(table, 4, 7, " -- ", "|fo|", "|o |", " -- ", "|ba|", "|r |", " -- ");
    assertRender(table, 4, 6, " -- ", "|fo|", "|o |", "|  |", "|  |", " -- ");
    assertRender(table, 4, 5, " -- ", "|fo|", "|o |", "|  |", " -- ");
    assertNoRender(table, 4, 4);
  }

  public void testRowLayoutWithOverflowHidden() {
    TableElement table = new TableElement().border(BorderStyle.DASHED).overflow(Overflow.HIDDEN);
    table.add(new RowElement(true).add(new LabelElement("foo")));
    table.add(new RowElement().add(new LabelElement("bar")));

    //
    assertRender(table, 5, 5, " --- ", "|foo|", " --- ", "|bar|", " --- ");
    assertRender(table, 5, 4, " --- ", "|foo|", "|   |", " --- ");
    assertNoRender(table, 5, 3);

    //
    assertRender(table, 4, 6, " -- ", "|fo|", " -- ", "|ba|", "|  |", " -- ");
    assertRender(table, 4, 5, " -- ", "|fo|", " -- ", "|ba|", " -- ");
    assertRender(table, 4, 4, " -- ", "|fo|", "|  |", " -- ");
    assertNoRender(table, 4, 3); // It should work with better impl
  }

  public void testRowLayoutWithColumns() {
    TableElement table = new TableElement().border(BorderStyle.DASHED).separator(BorderStyle.DASHED);
    table.add(new RowElement().add(new LabelElement("foo"), new LabelElement("bar")));

    //
    assertRender(table, 9, 3, " ------- ", "|foo|bar|", " ------- ");
    assertRender(table, 9, 4, " ------- ", "|foo|bar|", "|   |   |", " ------- ");
  }
}
