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

package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.shell.TestInvocationContext;
import org.crsh.shell.io.ShellFormatter;
import org.crsh.text.ChunkBuffer;
import org.crsh.text.Color;
import org.crsh.text.Decoration;

import static org.crsh.shell.ui.Element.*;

public class TableElementTestCase extends TestCase {

  public void testSimple() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        "a     b     _" +
        "c     d     _"
        , reader.toString());

  }

  public void testStyle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.
        add(row().
            background(Color.green).
            foreground(Color.blue).
            decoration(Decoration.bold).
            add(label("a")).
            add(label("b")))
      .add(row().
          add(label("c").
              background(Color.green).
              foreground(Color.blue).
              decoration(Decoration.bold)).
          add(label("d")));

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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo_" +
        "+-a     b     _" +
        "| c     d     _" +
        "+-bar_"
        , reader.toString());

  }

  public void testInNodeBorder() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo_" +
        "+- ---------------_" +
        "| | a     | b     |_" +
        "| | c     | d     |_" +
        "|  ---------------_" +
        "+-bar_"
        , reader.toString());

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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo_" +
        "+-a     b     c is a very very ver_" +
        "|             y too long value    _" +
        "| d     e     f                   _" +
        "+-bar_"
        , reader.toString());

  }

  public void testInNodeTooLargeBorder() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo_" +
        "+- ------------------------------_" +
        "| | a     | b     | c is a very v|_" +
        "| |       |       | ery very too |_" +
        "| |       |       | long value   |_" +
        "| | d     | e     | f            |_" +
        "|  ------------------------------_" +
        "+-bar_"
        , reader.toString());

  }

  public void testInNodeHeader() throws Exception {

    TableElement tableElement = new TableElement().setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(new RowElement(true).
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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo_" +
        "+- ---------------_" +
        "| | a     | b     |_" +
        "| | c     | d     |_" +
        "|  ---------------_" +
        "| | e     | f     |_" +
        "|  ---------------_" +
        "| | g     | h     |_" +
        "| | i     | j     |_" +
        "|  ---------------_" +
        "+-bar_"
        , reader.toString());

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

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        "a     This text is very ver very_" +
        "      too large to be displayed _" +
        "      in a cell of 32           _" +
        "c     d                         _"
        , reader.toString());
  }
  
  public void testSimpleBorder() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ---------------_" +
        "| a     | b     |_" +
        "| c     | d     |_" +
        " ---------------_"
        , reader.toString());

  }

  public void testBorderHeaderTopBottom() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.setBorder(true);

    tableElement.
        add(new RowElement(true).
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(row().
            add(label("e")).
            add(label("f"))).
        add(new RowElement(true).
            add(label("g")).
            add(label("h")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ---------------_" +
        "| a     | b     |_" +
        " ---------------_" +
        "| c     | d     |_" +
        "| e     | f     |_" +
        " ---------------_" +
        "| g     | h     |_" +
        " ---------------_"
        , reader.toString());

  }

  public void testNoBorderHeaderTopBottom() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.setBorder(false);

    tableElement.
        add(new RowElement(true).
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(row().
            add(label("e")).
            add(label("f")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        "a     b     _" +
        "c     d     _" +
        "e     f     _"
        , reader.toString());

  }

  public void testBorderHeaderMiddle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(new RowElement(true).
            add(label("e")).
            add(label("f"))).
        add(row().
            add(label("g")).
            add(label("h"))).
        add(row().
            add(label("i")).
            add(label("j")));


    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ---------------_" +
        "| a     | b     |_" +
        "| c     | d     |_" +
        " ---------------_" +
        "| e     | f     |_" +
        " ---------------_" +
        "| g     | h     |_" +
        "| i     | j     |_" +
        " ---------------_"
        , reader.toString());

  }

  public void testBorderHeaderTwoMiddle() throws Exception {

    TableElement tableElement = new TableElement();

    tableElement.setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c")).
            add(label("d"))).
        add(new RowElement(true).
            add(label("e")).
            add(label("f"))).
        add(new RowElement(true).
            add(label("g")).
            add(label("h"))).
        add(row().
            add(label("i")).
            add(label("j"))).
        add(row().
            add(label("k")).
            add(label("l")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ---------------_" +
        "| a     | b     |_" +
        "| c     | d     |_" +
        " ---------------_" +
        "| e     | f     |_" +
        " ---------------_" +
        "| g     | h     |_" +
        " ---------------_" +
        "| i     | j     |_" +
        "| k     | l     |_" +
        " ---------------_"
        , reader.toString());

  }

  public void testTooLargeBorder() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32"))).
        add(row().
            add(label("c")).
            add(label("d")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ------------------------------_" +
        "| a     | This text is very ver|_" +
        "|       | very too large to be |_" +
        "|       | displayed in a cell o|_" +
        "|       | f 32                 |_" +
        "| c     | d                    |_" +
        " ------------------------------_"
        , reader.toString());
  }

  public void testTooLargeBorderHeader() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

    tableElement.
        add(new RowElement(true).
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32"))).
        add(row().
            add(label("c")).
            add(label("d")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        " ------------------------------_" +
        "| a     | This text is very ver|_" +
        "|       | very too large to be |_" +
        "|       | displayed in a cell o|_" +
        "|       | f 32                 |_" +
        " ------------------------------_" +
        "| c     | d                    |_" +
        " ------------------------------_"
        , reader.toString());
  }

  public void testBorderStyle() throws Exception {

    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

    tableElement.
        add(row().
            background(Color.green).
            foreground(Color.blue).
            decoration(Decoration.bold).
            add(label("a")).
            add(label("b"))).
        add(row().
            add(label("c").
                background(Color.green).
                foreground(Color.blue).
                decoration(Decoration.bold)).
            add(label("d")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    String expected =
      " ---------------_|" +
      "\u001B[1;34;42m a     \u001B[0m|\u001B[1;34;42m b     \u001B[0m|_" +
      "|\u001B[1;34;42m c     \u001B[0m| \u001B[0md     \u001B[0m|_" +
      " ---------------_";

    StringBuilder sb = new StringBuilder();
    reader.writeAnsiTo(sb);
    String ansi = sb.toString();

    //
    assertEquals(
      expected
      , ansi);

  }

  public void testTooManyColumns() throws Exception {
    TableElement tableElement = new TableElement();
    tableElement.setBorder(true);

    tableElement.
        add(row().
            add(label("a")).
            add(label("This text is very ver very too large to be displayed in a cell of 32")).
            add(label("b")));

    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());
    assertEquals(
        " ------------------------------_" +
        "| a     | This text is very ver|_" +
        "|       | very too large to be |_" +
        "|       | displayed in a cell o|_" +
        "|       | f 32                 |_" +
        " ------------------------------_"
        , reader.toString());
  }

}
