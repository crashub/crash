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
package org.crsh.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FilterCommandTestCase extends AbstractCommandTestCase {

  public static List<Map> output = new ArrayList<Map>();

  private final String consume_command = "class consume_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public org.crsh.command.PipeCommand<java.util.Map, Object> main() {\n" +
      "return new org.crsh.command.PipeCommand<java.util.Map, Object>() {\n" +
      "public void provide(java.util.Map element) {\n" +
      "org.crsh.shell.FilterCommandTestCase.output.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  private final String produce_command = "class produce_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main(org.crsh.command.InvocationContext<java.util.Map> context) {\n" +
      "java.util.Map m = new java.util.HashMap<String, String>();\n" +
      "m.put(\"A\", \"A\");\n" +
      "m.put(\"B\", \"C\");\n" +
      "java.util.Map m2 = new java.util.HashMap<String, String>();\n" +
      "m2.put(\"A\", \"B\");\n" +
      "m2.put(\"B\", \"B\");\n" +
      "java.util.Map m3 = new java.util.HashMap<String, String>();\n" +
      "m3.put(\"A\", \"C\");\n" +
      "m3.put(\"B\", \"A\");\n" +
      "context.provide(m);\n" +
      "context.provide(m2);\n" +
      "context.provide(m3);\n" +
      "}\n" +
      "}";
  
  public void testSimple() throws Exception {
    output.clear();
    lifeCycle.bind("consume_command", consume_command);
    lifeCycle.bind("produce_command", produce_command);
    assertOk("produce_command | filter -e A:C | consume_command");
    assertEquals(1, output.size());
    assertEquals("C", output.get(0).get("A"));
  }

  public void testMany() throws Exception {
    output.clear();
    lifeCycle.bind("consume_command", consume_command);
    lifeCycle.bind("produce_command", produce_command);
    assertOk("produce_command | filter -e A:C -e A:B | consume_command");
    assertEquals(2, output.size());
    assertEquals("B", output.get(0).get("A"));
    assertEquals("C", output.get(1).get("A"));
  }

  public void testInvalid() throws Exception {
    output.clear();
    lifeCycle.bind("consume_command", consume_command);
    lifeCycle.bind("produce_command", produce_command);
    assertOk("produce_command | filter -e invalid | consume_command");
    assertEquals(3, output.size());
  }

  public void testIntersect() throws Exception {
    output.clear();
    lifeCycle.bind("consume_command", consume_command);
    lifeCycle.bind("produce_command", produce_command);
    assertOk("produce_command | filter -e A:C -e B:A | consume_command");
    assertEquals(1, output.size());
  }

}
