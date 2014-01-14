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

import java.util.Arrays;
import java.util.Collections;

public class PipeTestCase extends AbstractCommandTestCase {

  public void testClose() {
    lifeCycle.bindClass("closed", Commands.IsClosed.class);

    //
    Commands.IsClosed.closed.set(0);
    assertEquals("", assertOk("closed"));
    assertEquals(1, Commands.IsClosed.closed.get());

    //
    Commands.IsClosed.closed.set(0);
    assertEquals("", assertOk("echo abc | closed"));
    assertEquals(1, Commands.IsClosed.closed.get());
  }

  public void testIsPiped() {
    lifeCycle.bindClass("piped", Commands.IsPiped.class);
    lifeCycle.bindClass("produce_command", Commands.ProduceString.class);
    lifeCycle.bindClass("noop", Commands.Noop.class);

    //
    Commands.list.clear();
    assertEquals("", assertOk("piped"));
    assertEquals(Arrays.<Object>asList(Boolean.FALSE), Commands.list);
    Commands.list.clear();
    assertEquals("", assertOk("produce_command | piped"));
    assertEquals(Arrays.<Object>asList(Boolean.TRUE), Commands.list);
    Commands.list.clear();
    assertEquals("", assertOk("piped | noop"));
    assertEquals(Arrays.<Object>asList(Boolean.FALSE), Commands.list);
  }

  public void testKeepLastPipeContent() throws Exception {
    assertEquals("bar", assertOk("echo foo | echo bar"));
  }

  public void testFlushInPipe() throws Exception {
    assertEquals("juu", assertOk("echo -f 1 foo bar | echo juu"));
  }

  public void testProducerUseWriter() throws Exception {
    String cmd = "class foo {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.getWriter().print('foo');\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("cmd", cmd);

    //
    assertEquals("foo", assertOk("cmd"));
  }

  public void testProducerWithFormatter() throws Exception {
    lifeCycle.bindClass("cmd", Commands.ProduceValue.class);
    assertEquals("<value>abc</value>              \n", assertOk("cmd"));
  }

  public void testLeftShiftOperator() {
    String producer = "class foo {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context << 'hello';\n" +
        "context << 3;\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("producer", producer);
    lifeCycle.bindClass("consumer", Commands.ConsumeInteger.class);

    //
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.<Object>asList(3), Commands.list);
  }

  public void testAdaptToChunk() {
    lifeCycle.bindClass("producer", Commands.ProduceValue.class);
    lifeCycle.bindClass("consumer", Commands.ConsumeChunk.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    ChunkBuffer buffer = new ChunkBuffer().append(Commands.list);
    assertEquals("<value>abc</value>              \n", buffer.toString());
  }

  public void testLifeCycle() throws Exception {
    String consumer =
        "class consumer {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Object, Object> main() {\n" +
        "    return new org.crsh.command.PipeCommand<Object, Object>() {\n" +
        "      public void open() {\n" +
        "        org.crsh.shell.Commands.list.add('open');\n" +
        "      }\n" +
        "      public void close() {\n" +
        "        org.crsh.shell.Commands.list.add('close');\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}";

    //
    lifeCycle.bindClass("producer", Commands.Noop.class);
    lifeCycle.bindGroovy("consumer", consumer);

    //
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.asList("open"), Commands.list);
  }

  public void testPropagateFlush() throws Exception {
    String producer =
        "class producer {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Object, Object> main() {\n" +
        "    return new org.crsh.command.PipeCommand<Object, Object>() {\n" +
        "      public void open() {\n" +
        "        context.flush();\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}";
    String consumer =
        "class consumer {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Object, Object> main() {\n" +
        "    return new org.crsh.command.PipeCommand<Object, Object>() {\n" +
        "      public void flush() {\n" +
        "        org.crsh.shell.Commands.list.add('flush');\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}";

    //
    lifeCycle.bindGroovy("producer", producer);
    lifeCycle.bindGroovy("consumer", consumer);

    //
    Commands.list.clear();
    assertOk("producer | consumer");

    // Producer flush
    // Before close flush
    assertEquals(Arrays.asList("flush", "flush"), Commands.list);
  }

  public void testProvideInFlush() throws Exception {
    String producer =
        "class producer {\n" +
        "  @Command\n" +
        "  public org.crsh.command.PipeCommand<Object, String> main() {\n" +
        "    return new org.crsh.command.PipeCommand<Object, String>() {\n" +
        "      public void flush() {\n" +
        "        context.provide('foo');\n" +
        "      }\n" +
        "    };\n" +
        "  }\n" +
        "}";

    //
    lifeCycle.bindGroovy("producer", producer);
    lifeCycle.bindClass("consumer", Commands.ConsumeString.class);

    //
    Commands.list.clear();
    assertOk("producer | consumer");

    //
    assertEquals(Arrays.asList("foo"), Commands.list);
  }

  public void testProvideInClose() throws Exception {
    String producer =
        "class producer {\n" +
            "  @Command\n" +
            "  public org.crsh.command.PipeCommand<Object, String> main() {\n" +
            "    return new org.crsh.command.PipeCommand<Object, String>() {\n" +
            "      public void close() {\n" +
            "        context.provide('foo');\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}";

    //
    lifeCycle.bindGroovy("producer", producer);
    lifeCycle.bindClass("consumer", Commands.ConsumeString.class);

    //
    Commands.list.clear();
    assertOk("producer | consumer");

    //
    assertEquals(Arrays.asList("foo"), Commands.list);
  }

  public void testNotAssignableType() throws Exception {
    lifeCycle.bindClass("producer", Commands.ProduceInteger.class);
    lifeCycle.bindClass("consumer", Commands.ConsumeBoolean.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Collections.emptyList(), Commands.list);
  }

  public void testSameType() throws Exception {
    lifeCycle.bindClass("producer", Commands.ProduceInteger.class);
    lifeCycle.bindClass("consumer", Commands.ConsumeInteger.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.<Object>asList(3), Commands.list);
  }

  public void testConsumerThrowsScriptExceptionInProvide() throws Exception {
    String consumer =
        "class producer {\n" +
            "  @Command\n" +
            "  public org.crsh.command.PipeCommand<Integer, Object> main() {\n" +
            "    return new org.crsh.command.PipeCommand<Integer, Object>() {\n" +
            "      public void provide(Integer element) {\n" +
            "        throw new org.crsh.command.ScriptException('foo')\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}\n";
    lifeCycle.bindClass("producer", Commands.ProduceInteger.class);
    lifeCycle.bindGroovy("consumer", consumer);
    Commands.list.clear();
    Throwable t = assertError("producer | consumer", ErrorType.EVALUATION);
    ScriptException ex = assertInstance(ScriptException.class, t);
    assertEquals("foo", ex.getMessage());
  }

  public void testBuffer() {
    lifeCycle.bindClass("produce_command", Commands.ProduceString.class);
    lifeCycle.bindClass("b", Commands.Buffer.class);
    lifeCycle.bindClass("consume_command", Commands.ConsumeString.class);
    Commands.list.clear();
    assertOk("produce_command | consume_command");
    assertEquals(2, Commands.list.size());
    Commands.list.clear();
    assertOk("produce_command | b | consume_command");
    assertEquals(2, Commands.list.size());
  }

  public void testFilter() {
    lifeCycle.bindClass("produce_command", Commands.ProduceString.class);
    lifeCycle.bindClass("f", Commands.Filter.class);
    lifeCycle.bindClass("consume_command", Commands.ConsumeString.class);
    Commands.list.clear();
    assertOk("produce_command | consume_command");
    assertEquals(2, Commands.list.size());
    Commands.list.clear();
    assertOk("produce_command | f | consume_command");
    assertEquals(2, Commands.list.size());
  }

  public void testPipeEOL() {
    assertError("command |", ErrorType.EVALUATION);
  }
}
