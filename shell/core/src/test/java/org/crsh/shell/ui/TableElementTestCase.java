package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.shell.TestInvocationContext;
import org.crsh.term.ANSIFontBuilder;
import org.crsh.text.Color;
import org.crsh.text.Data;
import org.crsh.text.DataFragment;
import org.crsh.text.FormattingData;
import org.crsh.util.LineFeedWriter;

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

    Data data = new Data();
    LineFeedWriter writer = new LineFeedWriter(data, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
      "a     b     _c     d     _"
      , data.toString());

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

    Data data = new Data();
    LineFeedWriter writer = new LineFeedWriter(data, "_");

    tableElement.print(writer, new TestInvocationContext());

    String ansi = "\u001B[1;34;42ma\u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42mb\u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m_\u001B[1;34;42mc\u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0m\u001B[1;34;42m \u001B[0md \u001B[0m \u001B[0m \u001B[0m \u001B[0m \u001B[0m_";

    assertEquals(
      ansi
      , toAnsi(data));

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

    Data data = new Data();
    LineFeedWriter writer = new LineFeedWriter(data, "_");

    node.print(writer, new TestInvocationContext());

    assertEquals(
      "+-foo_+-a     b     _| c     d     _+-bar_"
      , data.toString());

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

    Data data = new Data();
    LineFeedWriter writer = new LineFeedWriter(data, "_");

    tableElement.print(writer, new TestInvocationContext());

    assertEquals(
      "a     This text is very ver very too large to be displayed in a cell of 32_c     d                         _"
      , data.toString());
  }

  public String toAnsi(Data data) {

    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();

    StringBuffer sb = new StringBuffer();

    for (DataFragment fragment : data) {
      if (fragment instanceof FormattingData) {
        sb.append(ansiFontBuilder.build((FormattingData) fragment));
      } else {
        sb.append(fragment.toString());
      }
    }

    return sb.toString();
  }

}
