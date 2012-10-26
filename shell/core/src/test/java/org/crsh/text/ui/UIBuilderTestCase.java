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

import groovy.lang.GroovyShell;
import org.crsh.AbstractTestCase;
import org.crsh.text.Color;
import org.crsh.text.Decoration;

import java.util.Arrays;
import java.util.List;

public class UIBuilderTestCase extends AbstractTestCase {

  public UIBuilderTestCase() {
  }

  public UIBuilderTestCase(String name) {
    super(name);
  }

/*
   public void testText()
   {
      GroovyShell shell = new GroovyShell();
      MessageElement res = (MessageElement)shell.evaluate(
         "import org.crsh.console.ConsoleBuilder;\n" +
         "def builder = new ConsoleBuilder();\n" +
         "return builder.message('some_text');\n"
      );
      assertEquals("some_text", res.getData());
   }
*/

  public void testEmptyTable() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "return builder;\n"
    );
    assertEquals(0, res.getElements().size());
  }

  public void testNode() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.node { };\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertNull(((TreeElement)res.getElements().get(0)).getValue());
    assertTrue(res.getElements().get(0) instanceof TreeElement);
    assertEquals(0, ((TreeElement)res.getElements().get(0)).getSize());
  }

  public void testLabelledNode() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.node('foo') { };\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TreeElement);
    assertTrue(((TreeElement)res.getElements().get(0)).getValue() instanceof LabelElement);
    assertEquals("foo", ((LabelElement)((TreeElement)res.getElements().get(0)).getValue()).getValue());
    assertEquals(0, ((TreeElement)res.getElements().get(0)).getSize());
  }

  public void testLabel() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.label('foo');\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof LabelElement);
    assertEquals("foo", ((LabelElement)res.getElements().get(0)).getValue());
  }

/*
   public void testTable1()
   {
      GroovyShell shell = new GroovyShell();
      TableElement res = (TableElement)shell.evaluate(
         "import org.crsh.console.ConsoleBuilder;\n" +
         "def builder = new ConsoleBuilder();\n" +
         "return builder.table {\n" +
         "row()\n" +
         "};\n"
      );
      assertEquals(1, res.getData().size());
      Row row = res.getData().get(0);
      assertEquals(Arrays.<String>asList(), row.getValues());
   }

   public void testTable2()
   {
      GroovyShell shell = new GroovyShell();
      TableElement res = (TableElement)shell.evaluate(
         "import org.crsh.console.ConsoleBuilder;\n" +
         "def builder = new ConsoleBuilder();\n" +
         "return builder.table {\n" +
         "row(['foo','bar'])\n" +
         "};\n"
      );
      assertEquals(1, res.getData().size());
      Row row = res.getData().get(0);
      assertEquals(Arrays.asList("foo", "bar"), row.getValues());
   }

   public void testTable3()
   {
      GroovyShell shell = new GroovyShell();
      TableElement res = (TableElement)shell.evaluate(
         "import org.crsh.console.ConsoleBuilder;\n" +
         "def table = [['a','b'],['c','d']];\n" +
         "def builder = new ConsoleBuilder();\n" +
         "return builder.table {\n" +
         "table.each { array -> \n" +
         " row([array[0],array[1]])\n" +
         "}\n" +
         "};\n"
      );
      assertEquals(2, res.getData().size());
      Row row0 = res.getData().get(0);
      Row row1 = res.getData().get(1);
      assertEquals(Arrays.asList("a", "b"), row0.getValues());
      assertEquals(Arrays.asList("c", "d"), row1.getValues());
   }

  public void testTable4()
  {
     GroovyShell shell = new GroovyShell();
     TableElement res = (TableElement)shell.evaluate(
        "import org.crsh.console.ConsoleBuilder;\n" +
        "def table = [['a','b'],['c','d']];\n" +
        "def builder = new ConsoleBuilder();\n" +
        "return builder.table {\n" +
        "  table.each { array -> \n" +
        "    row {" +
        "      array.each {" +
        "        value -> cell(value)" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "};\n"
     );
     assertEquals(2, res.getData().size());
     Row row0 = res.getData().get(0);
     Row row1 = res.getData().get(1);
     assertEquals(Arrays.asList("a", "b"), row0.getValues());
     assertEquals(Arrays.asList("c", "d"), row1.getValues());
  }

   public void testElements()
   {
      GroovyShell shell = new GroovyShell();
      List<ConsoleElement> elements = (List<ConsoleElement>)shell.evaluate(
         "import org.crsh.console.ConsoleBuilder;\n" +
         "def builder = new ConsoleBuilder();\n" +
         "builder.message('a');\n" +
         "builder.message('b');\n" +
         "return builder.elements;\n"
      );
      assertEquals(2, elements.size());
      assertEquals("a", ((MessageElement)elements.get(0)).getData());
      assertEquals("b", ((MessageElement)elements.get(1)).getData());
   }
*/

  public void testTable() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table { };\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertEquals(0, ((TableElement)res.getElements().get(0)).getRows().size());
  }

  public void testEmptyRow() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "row { }\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertEquals(1, ((TableElement)res.getElements().get(0)).getRows().size());
    assertEquals(0, ((TableElement)res.getElements().get(0)).getRows().get(0).getSize());
  }

  public void testRow() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "row () {\n" +
            "label(\"col1\"); label(\"col2\")\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertEquals(1, ((TableElement)res.getElements().get(0)).getRows().size());
    assertEquals(2, ((TableElement)res.getElements().get(0)).getRows().get(0).getSize());
    assertEquals("Label[col1]", ((TableElement)res.getElements().get(0)).getRows().get(0).getCol(0).toString());
    assertEquals("Label[col2]", ((TableElement)res.getElements().get(0)).getRows().get(0).getCol(1).toString());
  }

  public void testRowStyleWithEnd() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
      "import org.crsh.text.Color;\n" +
      "import org.crsh.text.Style;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "row (bold: true, foreground: red, background: green) {\n" +
            "label(\"col1\"); label(\"col2\")\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    TableElement table = assertInstance(TableElement.class, res.getElements().get(0));
    assertEquals(1, table.getRows().size());
    assertEquals(2, table.getRows().get(0).getSize());
    assertEquals(Decoration.bold.fg(Color.red).bg(Color.green), table.getRows().get(0).getStyle());
  }

  public void testTableCols() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
            "def builder = new UIBuilder();\n" +
            "builder.table(columns: [1,1]) {\n" +
            "row {\n" +
            "}\n" +
            "};\n" +
            "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    TableElement table = assertInstance(TableElement.class, res.getElements().get(0));
    Layout.Weighted layout = assertInstance(Layout.Weighted.class, table.getColumnLayout());
    assertTrue(Arrays.equals(new int[]{1,1}, layout.getWeights()));
  }

  public void testTableBorderTrue() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table(border: true) {\n" +
          "row {\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertSame(BorderStyle.DASHED, ((TableElement)res.getElements().get(0)).border);
  }

  public void testTableBorderFalse() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
            "def builder = new UIBuilder();\n" +
            "builder.table(border: false) {\n" +
            "row {\n" +
            "}\n" +
            "};\n" +
            "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertSame(null, ((TableElement)res.getElements().get(0)).border);
  }

  public void testTableBorderNull() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
            "def builder = new UIBuilder();\n" +
            "builder.table(border: null) {\n" +
            "row {\n" +
            "}\n" +
            "};\n" +
            "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertSame(null, ((TableElement)res.getElements().get(0)).border);
  }

  public void testTableBorderDash() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table(border:org.crsh.text.ui.BorderStyle.DASHED) {\n" +
        "row {\n" +
        "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertSame(BorderStyle.DASHED, ((TableElement)res.getElements().get(0)).border);
  }

  public void testTableBorderStar() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table(border: org.crsh.text.ui.BorderStyle.STAR) {\n" +
        "row {\n" +
        "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertSame(BorderStyle.STAR, ((TableElement)res.getElements().get(0)).border);
  }

  public void testTableHeader() {
    GroovyShell shell = new GroovyShell();
    UIBuilder res = (UIBuilder)shell.evaluate(
      "import org.crsh.text.ui.UIBuilder;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "header () {\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
    );
    assertEquals(1, res.getElements().size());
    assertTrue(res.getElements().get(0) instanceof TableElement);
    assertEquals(1, ((TableElement)res.getElements().get(0)).getRows().size());
    assertEquals(true, ((TableElement)res.getElements().get(0)).getRows().get(0).header);
  }

  public void testForbiddenChild() throws Exception {
    GroovyShell shell = new GroovyShell();

    try {
      shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
        "import org.crsh.text.Color;\n" +
        "import org.crsh.text.Style;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "row() {\n" +
            "node()\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
      );
      fail();
    } catch (IllegalArgumentException iae) {
      assertEquals("A table cannot contain a tree element", iae.getMessage());
    }
  }

  public void testEval() {
    GroovyShell shell = new GroovyShell();

    UIBuilder builder = (UIBuilder)shell.evaluate(
        "import org.crsh.text.ui.UIBuilder;\n" +
        "import org.crsh.text.Color;\n" +
        "import org.crsh.text.Style;\n" +
        "def builder = new UIBuilder();\n" +
        "builder.table {\n" +
          "row {\n" +
            "eval {" +
            " return 'HELLO';" +
            "}\n" +
          "}\n" +
        "};\n" +
        "return builder;\n"
    );

    //
    List<Element> elements = builder.getElements();
    assertEquals(1, elements.size());
    TableElement table = assertInstance(TableElement.class, elements.get(0));
    assertEquals(1, table.getRows().size());
    RowElement row = table.getRows().get(0);
    assertEquals(1, row.getSize());
    EvalElement eval = assertInstance(EvalElement.class, row.getCol(0));
    assertNotNull(eval.closure);
    assertEquals("HELLO", eval.closure.call());
  }
}
