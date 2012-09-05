package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.shell.TestInvocationContext;
import org.crsh.text.ChunkSequence;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.shell.io.ShellWriter;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class TableElementTestCase extends TestCase {

  public void testSimple() throws Exception {

    TableElement tableElement = new TableElement();

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));
    
    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
        "a     b     _" +
        "c     d     _"
        , reader.toString());

  }

  public void testStyle() throws Exception {

    TableElement tableElement = new TableElement();

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.setBackground(Color.green);
    row1.setForeground(Color.blue);
    row1.setDecoration(Decoration.bold);
    LabelElement a = new LabelElement("a");
    a.setParent(row1);
    LabelElement b = new LabelElement("b");
    b.setParent(row1);
    row1.addValue(a);
    row1.addValue(b);

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    LabelElement c = new LabelElement("c");
    c.setParent(row2);
    c.setBackground(Color.green);
    c.setForeground(Color.blue);
    c.setDecoration(Decoration.bold);
    row2.addValue(c);
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    TreeElement node = new TreeElement();
    node.addNode(new LabelElement("foo"));
    node.addNode(tableElement);
    node.addNode(new LabelElement("bar"));

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    TreeElement node = new TreeElement();
    node.addNode(new LabelElement("foo"));
    node.addNode(tableElement);
    node.addNode(new LabelElement("bar"));

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));
    row1.addValue(new LabelElement("c is a very very very too long value"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("d"));
    row2.addValue(new LabelElement("e"));
    row2.addValue(new LabelElement("f"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    TreeElement node = new TreeElement();
    node.addNode(new LabelElement("foo"));
    node.addNode(tableElement);
    node.addNode(new LabelElement("bar"));

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));
    row1.addValue(new LabelElement("c is a very very very too long value"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("d"));
    row2.addValue(new LabelElement("e"));
    row2.addValue(new LabelElement("f"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    TreeElement node = new TreeElement();
    node.addNode(new LabelElement("foo"));
    node.addNode(tableElement);
    node.addNode(new LabelElement("bar"));

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    TableElement tableElement = new TableElement();

    tableElement.setBorder(true);

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    RowElement row3 = new RowElement(true);
    row3.setParent(tableElement);
    row3.addValue(new LabelElement("e"));
    row3.addValue(new LabelElement("f"));

    RowElement row4 = new RowElement();
    row4.setParent(tableElement);
    row4.addValue(new LabelElement("g"));
    row4.addValue(new LabelElement("h"));

    RowElement row5 = new RowElement();
    row5.setParent(tableElement);
    row5.addValue(new LabelElement("i"));
    row5.addValue(new LabelElement("j"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);
    tableElement.addRow(row3);
    tableElement.addRow(row4);
    tableElement.addRow(row5);

    TreeElement node = new TreeElement();
    node.addNode(new LabelElement("foo"));
    node.addNode(tableElement);
    node.addNode(new LabelElement("bar"));

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("This text is very ver very too large to be displayed in a cell of 32"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement(true);
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    RowElement row3 = new RowElement();
    row3.setParent(tableElement);
    row3.addValue(new LabelElement("e"));
    row3.addValue(new LabelElement("f"));

    RowElement row4 = new RowElement(true);
    row4.setParent(tableElement);
    row4.addValue(new LabelElement("g"));
    row4.addValue(new LabelElement("h"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);
    tableElement.addRow(row3);
    tableElement.addRow(row4);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement(true);
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    RowElement row3 = new RowElement();
    row3.setParent(tableElement);
    row3.addValue(new LabelElement("e"));
    row3.addValue(new LabelElement("f"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);
    tableElement.addRow(row3);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    RowElement row3 = new RowElement(true);
    row3.setParent(tableElement);
    row3.addValue(new LabelElement("e"));
    row3.addValue(new LabelElement("f"));

    RowElement row4 = new RowElement();
    row4.setParent(tableElement);
    row4.addValue(new LabelElement("g"));
    row4.addValue(new LabelElement("h"));

    RowElement row5 = new RowElement();
    row5.setParent(tableElement);
    row5.addValue(new LabelElement("i"));
    row5.addValue(new LabelElement("j"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);
    tableElement.addRow(row3);
    tableElement.addRow(row4);
    tableElement.addRow(row5);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("b"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    RowElement row3 = new RowElement(true);
    row3.setParent(tableElement);
    row3.addValue(new LabelElement("e"));
    row3.addValue(new LabelElement("f"));

    RowElement row4 = new RowElement(true);
    row4.setParent(tableElement);
    row4.addValue(new LabelElement("g"));
    row4.addValue(new LabelElement("h"));

    RowElement row5 = new RowElement();
    row5.setParent(tableElement);
    row5.addValue(new LabelElement("i"));
    row5.addValue(new LabelElement("j"));

    RowElement row6 = new RowElement();
    row6.setParent(tableElement);
    row6.addValue(new LabelElement("k"));
    row6.addValue(new LabelElement("l"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);
    tableElement.addRow(row3);
    tableElement.addRow(row4);
    tableElement.addRow(row5);
    tableElement.addRow(row6);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("This text is very ver very too large to be displayed in a cell of 32"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement(true);
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("This text is very ver very too large to be displayed in a cell of 32"));

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    row2.addValue(new LabelElement("c"));
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.setBackground(Color.green);
    row1.setForeground(Color.blue);
    row1.setDecoration(Decoration.bold);
    LabelElement a = new LabelElement("a");
    a.setParent(row1);
    LabelElement b = new LabelElement("b");
    b.setParent(row1);
    row1.addValue(a);
    row1.addValue(b);

    RowElement row2 = new RowElement();
    row2.setParent(tableElement);
    LabelElement c = new LabelElement("c");
    c.setParent(row2);
    c.setBackground(Color.green);
    c.setForeground(Color.blue);
    c.setDecoration(Decoration.bold);
    row2.addValue(c);
    row2.addValue(new LabelElement("d"));

    tableElement.addRow(row1);
    tableElement.addRow(row2);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

    tableElement.print(writer, new TestInvocationContext());

    String expected = " ---------------_|\u001B[1;34;42m a     \u001B[0m|\u001B[1;34;42m b     \u001B[0m|_|\u001B[1;34;42m c     \u001B[0m| \u001B[0md     \u001B[0m|_ ---------------_";

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

    RowElement row1 = new RowElement();
    row1.setParent(tableElement);
    row1.addValue(new LabelElement("a"));
    row1.addValue(new LabelElement("This text is very ver very too large to be displayed in a cell of 32"));
    row1.addValue(new LabelElement("b"));

    tableElement.addRow(row1);

    ChunkSequence reader = new ChunkSequence();
    ShellWriter writer = new ShellWriter(reader, "_");

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
