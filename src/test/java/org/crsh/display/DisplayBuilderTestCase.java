/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.display;

import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.crsh.display.structure.LabelElement;
import org.crsh.display.structure.TreeElement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DisplayBuilderTestCase extends TestCase {

  public DisplayBuilderTestCase() {
  }

  public DisplayBuilderTestCase(String name) {
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
    DisplayBuilder res = (DisplayBuilder)shell.evaluate(
      "import org.crsh.display.DisplayBuilder;\n" +
        "def builder = new DisplayBuilder();\n" +
        "return builder;\n"
    );
    assertEquals(0, res.getElements().size());
  }

  public void testNode() {
    GroovyShell shell = new GroovyShell();
    DisplayBuilder res = (DisplayBuilder)shell.evaluate(
      "import org.crsh.display.DisplayBuilder;\n" +
        "def builder = new DisplayBuilder();\n" +
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
    DisplayBuilder res = (DisplayBuilder)shell.evaluate(
      "import org.crsh.display.DisplayBuilder;\n" +
        "def builder = new DisplayBuilder();\n" +
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
    DisplayBuilder res = (DisplayBuilder)shell.evaluate(
      "import org.crsh.display.DisplayBuilder;\n" +
        "def builder = new DisplayBuilder();\n" +
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

}