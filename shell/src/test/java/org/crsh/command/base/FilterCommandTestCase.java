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

import org.crsh.command.ScriptException;
import org.crsh.shell.AbstractShellTestCase;
import org.crsh.shell.ErrorKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.crsh.util.Utils.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FilterCommandTestCase extends AbstractShellTestCase {

  public static List<Map> output = new ArrayList<Map>();

  private final String consume_command = "class consume_command {\n" +
      "@Command\n" +
      "public org.crsh.command.Pipe<java.util.Map, Object> main() {\n" +
      "return new org.crsh.command.Pipe<java.util.Map, Object>() {\n" +
      "public void provide(java.util.Map element) {\n" +
      "org.crsh.command.base.FilterCommandTestCase.output.add(element)\n" +
      "}\n" +
      "}\n" +
      "}\n" +
      "}";

  private final String produce_command = "class produce_command {\n" +
      "@Command\n" +
      "public void main(org.crsh.command.InvocationContext<java.util.Map> context) {\n" +
      "context.provide([A:'A',B:'C']);\n" +
      "context.provide([A:'B',B:'B']);\n" +
      "context.provide([A:'C',B:'A']);\n" +
      "}\n" +
      "}";
  
  public void testSimple() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    lifeCycle.bindGroovy("produce_command", produce_command);
    assertOk("produce_command | filter -p A:C | consume_command");
    assertEquals(list(map(map("A", "C"), "B", "A")), output);
  }

  public void testMany() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    lifeCycle.bindGroovy("produce_command", produce_command);
    assertOk("produce_command | filter -p A:C -p A:B | consume_command");
    assertEquals(list(map(map("A", "B"), "B", "B"), map(map("A", "C"), "B", "A")), output);
  }

  public void testInvalid() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    lifeCycle.bindGroovy("produce_command", produce_command);
    assertError("produce_command | filter -p invalid | consume_command", ErrorKind.EVALUATION, ScriptException.class);
  }

  public void testIntersect() throws Exception {
    output.clear();
    lifeCycle.bindGroovy("consume_command", consume_command);
    lifeCycle.bindGroovy("produce_command", produce_command);
    assertOk("produce_command | filter -p A:C -p B:A | consume_command");
    assertEquals(list(map(map("A", "C"), "B", "A")), output);
  }

}
