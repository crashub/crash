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

  public void testIsPiped() {
    lifeCycle.bind("piped", Commands.IsPiped.class);
    lifeCycle.bind("produce_command", Commands.ProduceString.class);

    //
    Commands.list.clear();
    assertEquals("", assertOk("piped"));
    assertEquals(Arrays.asList(Boolean.FALSE), Commands.list);
    Commands.list.clear();
    assertEquals("", assertOk("produce_command | piped"));
    assertEquals(Arrays.asList(Boolean.TRUE), Commands.list);

    //
    lifeCycle.bind("inscript", "produce_command piped");
    Commands.list.clear();
    assertEquals("", assertOk("inscript"));
    assertEquals(Arrays.asList(Boolean.TRUE), Commands.list);
  }

  public void testKeepLastPipeContent() throws Exception {
    assertEquals("bar", assertOk("echo foo | echo bar"));
  }

  public void testFlushInPipe() throws Exception {
    assertEquals("juu", assertOk("echo -f 1 foo bar | echo juu"));
  }

  public void testProducerUseWriter() throws Exception {
    String cmd = "class foo extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public void main(org.crsh.command.InvocationContext<Integer> context) {\n" +
        "context.getWriter().print('foo');\n" +
        "}\n" +
        "}";
    lifeCycle.bind("cmd", cmd);

    //
    assertEquals("foo", assertOk("cmd"));
  }

  public void testProducerWithFormatter() throws Exception {
    lifeCycle.bind("cmd", Commands.ProduceValue.class);
    assertEquals("<value>abc</value>              \n", assertOk("cmd"));
  }

  public void testAdaptToChunk() {
    lifeCycle.bind("producer", Commands.ProduceValue.class);
    lifeCycle.bind("consumer", Commands.ConsumeChunk.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    ChunkBuffer buffer = new ChunkBuffer().append(Commands.list);
    assertEquals("<value>abc</value>              \n", buffer.toString());
  }

  public void testLifeCycle() throws Exception {
    String consumer =
        "class consumer extends org.crsh.command.CRaSHCommand {\n" +
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
    lifeCycle.bind("producer", Commands.Noop.class);
    lifeCycle.bind("consumer", consumer);

    //
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.asList("open"), Commands.list);
  }

  public void testPropagateFlush() throws Exception {
    String producer =
        "class producer extends org.crsh.command.CRaSHCommand {\n" +
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
        "class consumer extends org.crsh.command.CRaSHCommand {\n" +
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
    lifeCycle.bind("producer", producer);
    lifeCycle.bind("consumer", consumer);

    //
    Commands.list.clear();
    assertOk("producer | consumer");

    // Producer flush
    // Before close flush
    assertEquals(Arrays.asList("flush", "flush"), Commands.list);
  }

  public void testNotAssignableType() throws Exception {
    lifeCycle.bind("producer", Commands.ProduceInteger.class);
    lifeCycle.bind("consumer", Commands.ConsumeBoolean.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Collections.emptyList(), Commands.list);
  }

  public void testSameType() throws Exception {
    lifeCycle.bind("producer", Commands.ProduceInteger.class);
    lifeCycle.bind("consumer", Commands.ConsumeInteger.class);
    Commands.list.clear();
    assertOk("producer | consumer");
    assertEquals(Arrays.<Object>asList(3), Commands.list);
  }

  public void testConsumerThrowsScriptExceptionInProvide() throws Exception {
    String consumer =
        "class producer extends org.crsh.command.CRaSHCommand {\n" +
            "  @Command\n" +
            "  public org.crsh.command.PipeCommand<Integer, Object> main() {\n" +
            "    return new org.crsh.command.PipeCommand<Integer, Object>() {\n" +
            "      public void provide(Integer element) {\n" +
            "        throw new org.crsh.command.ScriptException('foo')\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}\n";
    lifeCycle.bind("producer", Commands.ProduceInteger.class);
    lifeCycle.bind("consumer", consumer);
    Commands.list.clear();
    Throwable t = assertError("producer | consumer", ErrorType.EVALUATION);
    ScriptException ex = assertInstance(ScriptException.class, t);
    assertEquals("foo", ex.getMessage());
  }
}
