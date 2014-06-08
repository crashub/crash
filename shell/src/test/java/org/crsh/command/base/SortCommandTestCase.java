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
package org.crsh.command.base;

import org.crsh.shell.AbstractShellTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SortCommandTestCase extends AbstractShellTestCase {

  public static List<Map> output = new ArrayList<Map>();

  private final String consume_command = "class consume_command {\n" +
      "@Command\n" +
      "public org.crsh.command.Pipe<java.util.Map, Object> main() {\n" +
      "return new org.crsh.command.Pipe<java.util.Map, Object>() {\n" +
      "public void provide(java.util.Map element) {\n" +
      "org.crsh.command.base.SortCommandTestCase.output.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  private final String produce_command = "class produce_command {\n" +
      "@Command\n" +
      "public void main(org.crsh.command.InvocationContext<java.util.Map> context) {\n" +
      "java.util.Map m = new java.util.HashMap<String, String>();\n" +
      "m.put(\"A\", \"A\");\n" +
      "m.put(\"B\", \"B\");\n" +
      "m.put(\"C\", \"C\");\n" +
      "java.util.Map m2 = new java.util.HashMap<String, String>();\n" +
      "m2.put(\"A\", \"A\");\n" +
      "m2.put(\"B\", \"A\");\n" +
      "m2.put(\"C\", \"B\");\n" +
      "java.util.Map m3 = new java.util.HashMap<String, String>();\n" +
      "m3.put(\"A\", \"C\");\n" +
      "m3.put(\"B\", \"B\");\n" +
      "m3.put(\"C\", \"A\");\n" +
      "context.provide(m);\n" +
      "context.provide(m2);\n" +
      "context.provide(m3);\n" +
      "}\n" +
      "}";

  public void testNone() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("C"));
  }

  public void testSimple() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort -f B | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("C"));
  }

  public void testAsc() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort -f B:asc | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("C"));
  }

  public void testDesc() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort -f B:desc | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("C"));
  }

  public void testMany() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort -f B -f C | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(2)).get("C"));
  }

  public void testInvalid() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("produce_command", produce_command);
    lifeCycle.bindGroovy("consume_command", consume_command);
    assertOk("produce_command | sort -f B -f invalid | consume_command");
    assertEquals(3, output.size());
    assertEquals("A", ((Map<String,String>)output.get(0)).get("A"));
    assertEquals("A", ((Map<String,String>)output.get(0)).get("B"));
    assertEquals("B", ((Map<String,String>)output.get(0)).get("C"));
    assertEquals("A", ((Map<String,String>)output.get(1)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(1)).get("B"));
    assertEquals("C", ((Map<String,String>)output.get(1)).get("C"));
    assertEquals("C", ((Map<String,String>)output.get(2)).get("A"));
    assertEquals("B", ((Map<String,String>)output.get(2)).get("B"));
    assertEquals("A", ((Map<String,String>)output.get(2)).get("C"));
  }


}
