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

public class PipeTestCase extends AbstractCommandTestCase {

  /** . */
  private final String produce_command = "class produce_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main(org.crsh.command.InvocationContext<Void, String> context) {\n" +
      "['foo','bar'].each { context.produce(it) }" +
      "}\n" +
      "}";

  public void testProduce() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main() {\n" +
        "produce_command { out << it }\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("produce_command", produce_command);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testProduceInScript() {
    String foo = "produce_command { out << it }\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("produce_command", produce_command);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testAggregateContent() throws Exception {
    assertEquals("foobar", assertOk("echo foo + echo bar"));
  }

  public void testKeepLastPipeContent() throws Exception {
    assertEquals("bar", assertOk("echo foo | echo bar"));
  }

  public void testFlushInPipe() throws Exception {
    assertEquals("foojuu", assertOk("echo -f 1 foo bar | echo juu"));
  }
}
