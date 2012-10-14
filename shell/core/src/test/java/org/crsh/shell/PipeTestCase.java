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

import org.crsh.command.ScriptException;
import org.crsh.text.ChunkBuffer;

import java.util.ArrayList;
import java.util.Arrays;

public class PipeTestCase extends AbstractCommandTestCase {

  /** . */
  private final String produce_command = "class produce_command extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main(org.crsh.command.InvocationContext<String> context) {\n" +
      "['foo','bar'].each { context.provide(it) }" +
      "}\n" +
      "}";

  public void testProduceToClosure() {
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

  public void testProduceToClosureInScript() {
    lifeCycle.setCommand("foo", "produce_command { out << it }\n");
    lifeCycle.setCommand("produce_command", produce_command);

    //
    assertEquals("foobar", assertOk("foo"));
  }

  public void testKeepLastPipeContent() throws Exception {
    assertEquals("bar", assertOk("echo foo | echo bar"));
  }

  public void testFlushInPipe() throws Exception {
    assertEquals("juu", assertOk("echo -f 1 foo bar | echo juu"));
  }

  public void testProducerCannotUseWriter() throws Exception {
    String cmd = "class foo extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.getWriter().print('foo');\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("cmd", cmd);

    //
    assertEquals("foo", assertOk("cmd"));
  }

  public void testProducerWithFormatter() throws Exception {
    String cmd = "class foo extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<org.crsh.shell.Foo> context) {\n" +
        "context.provide(new org.crsh.shell.Foo('abc'));\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("cmd", cmd);

    //
    assertEquals("<foo>abc</foo>                  \n", assertOk("cmd"));
  }

  public void testAdaptToChunk() {
    String producer = "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<org.crsh.shell.Foo> context) {\n" +
        "context.provide(new org.crsh.shell.Foo('abc'));\n" +
        "}\n" +
        "}";
    String consumer =
        "class consumer extends org.crsh.command.CRaSHCommand {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<org.crsh.text.Chunk> main() {\n" +
        "    return new org.crsh.command.AbstractPipeCommand<org.crsh.text.Chunk>() {\n" +
        "      public void provide(org.crsh.text.Chunk element) {\n" +
        "        org.crsh.shell.PipeTestCase.list.add(element);\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}";
    lifeCycle.setCommand("producer", producer);
    lifeCycle.setCommand("consumer", consumer);
    list.clear();
    assertOk("producer | consumer");
    ChunkBuffer buffer = new ChunkBuffer().append(list);
    assertEquals("<foo>abc</foo>                  \n", buffer.toString());
  }

  public void testIncompatibleType() throws Exception {
    String producer = "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.provide(3);\n" +
        "}\n" +
        "}";
    String consumer = "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Boolean> main() {\n" +
        "    return new org.crsh.command.AbstractPipeCommand<Boolean>() {\n" +
        "      public void provide(Boolean element) {\n" +
        "        throw new RuntimeException('Was not expecting invocation to work');\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}";
    lifeCycle.setCommand("producer", producer);
    lifeCycle.setCommand("consumer", consumer);
    assertOk("producer | consumer");
  }

  /** . */
  public static final ArrayList<?> list = new ArrayList<Object>();

  public void testProducerConsumer() throws Exception {
    String producer = "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.provide(3);\n" +
        "}\n" +
        "}";
    String consumer =
        "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Integer> main() {\n" +
        "    return new org.crsh.command.AbstractPipeCommand<Integer>() {\n" +
        "      int count = 0;\n" +
        "      public void provide(Integer element) {\n" +
        "        org.crsh.shell.PipeTestCase.list.add(element);\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}\n";
    lifeCycle.setCommand("producer", producer);
    lifeCycle.setCommand("consumer", consumer);
    list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.<Object>asList(3), list);
  }

  public void testProducerThrowsScriptExceptionInProvide() throws Exception {
    String producer = "class producer extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.provide(3);\n" +
        "}\n" +
        "}";
    String consumer =
        "class producer extends org.crsh.command.CRaSHCommand {\n" +
            "  @Command\n" +
            "  public org.crsh.command.PipeCommand<Integer> main() {\n" +
            "    return new org.crsh.command.AbstractPipeCommand<Integer>() {\n" +
            "      public void provide(Integer element) {\n" +
            "        throw new org.crsh.command.ScriptException('foo')\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}\n";
    lifeCycle.setCommand("producer", producer);
    lifeCycle.setCommand("consumer", consumer);
    list.clear();
    Throwable t = assertError("producer | consumer", ErrorType.EVALUATION);
    ScriptException ex = assertInstance(ScriptException.class, t);
    assertEquals("foo", ex.getMessage());
  }
}
