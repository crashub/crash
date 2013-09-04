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

import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseCommand;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.PipeCommand;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Commands {

  /** . */
  public static final ArrayList<Object> list = new ArrayList<Object>();

  public static class Noop extends BaseCommand {
    @Command
    public PipeCommand<Object, Object> main() throws IOException {
      return new PipeCommand<Object, Object>() {
      };
    }
  }

  public static class Parameterized extends BaseCommand {

    /** . */
    public static String opt;

    /** . */
    public static List<String> args;

    public static void reset() {
      opt = null;
      args = null;
    }

    @Command
    public void main(final @Option(names={"opt"}) String opt, @Argument List<String> args) {
      Parameterized.opt = opt;
      if (args != null) {
        Parameterized.args = new ArrayList<String>(args);
      }
    }
  }

  public static class ProduceValue extends BaseCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<Value> context) throws IOException {
      context.provide(new Value("abc"));
    }
  }

  public static class ProduceString extends BaseCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<String> context) throws IOException {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class ConsumeString extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<String, Object> main() {
      return new PipeCommand<String, Object>() {
        @Override
        public void provide(String element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class Buffer extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<String, String> main() {
      return new PipeCommand<String, String>() {
        List<String> buffer = new ArrayList<String>();
        @Override
        public void provide(String element) throws ScriptException, IOException {
          buffer.add(element);
        }
        @Override
        public void flush() throws ScriptException, IOException {
          for (String s : buffer) {
            context.provide(s);
          }
          buffer.clear();
          super.flush();
        }
      };
    }
  }

  public static class Filter extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<String, String> main() {
      return new PipeCommand<String, String>() {
        @Override
        public void provide(String element) throws ScriptException, IOException {
          context.provide(element);
        }
      };
    }
  }

  public static class ProduceInteger extends BaseCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<Integer> context) throws IOException {
      context.provide(3);
    }
  }

  public static class ConsumeInteger extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<Integer, Object> main() {
      return new PipeCommand<Integer, Object>() {
        @Override
        public void provide(Integer element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ConsumeBoolean extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<Boolean, Object> main() {
      return new PipeCommand<Boolean, Object>() {
        @Override
        public void provide(Boolean element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ConsumeChunk extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<Chunk, Object> main() {
      return new PipeCommand<Chunk, Object>() {
        @Override
        public void provide(Chunk element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ConsumeObject extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<Object, Object> main() {
      return new PipeCommand<Object, Object>() {
        @Override
        public void provide(Object element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ParameterizedConsumeToList extends BaseCommand {
    @Command
    public PipeCommand<String, Object> main(final @Option(names={"opt"}) String opt, @Argument List<String> args) {
      if (args != null) {
        for (String arg : args) {
          list.add((opt != null ? opt : "") + arg);
        }
      }
      return new PipeCommand<String, Object>() {
        @Override
        public void provide(String element) throws ScriptException, IOException {
          list.add((opt != null ? opt : "") + element);
        }
      };
    }
  }

  public static class IsClosed extends BaseCommand {

    /** . */
    public static final AtomicInteger closed = new AtomicInteger();

    @Command
    public org.crsh.command.PipeCommand<Object, Object> main() {
      return new PipeCommand<Object, Object>() {
        @Override
        public void close() throws ScriptException {
          closed.incrementAndGet();
        }
      };
    }
  }

  public static class IsPiped extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<Object, Object> main() {
      return new PipeCommand<Object, Object>() {
        @Override
        public void open() throws ScriptException {
          list.add(isPiped());
        }
      };
    }
  }

  public static class Compound extends BaseCommand {
    @Command
    public String compound() {
      return "bar";
    }
  }

  public static class CompoundProduceString extends BaseCommand {
    @Command
    public void compound(org.crsh.command.InvocationContext<String> context) throws IOException {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class CompoundConsumeString extends BaseCommand {
    @Command
    public org.crsh.command.PipeCommand<String, Object> compound() {
      return new PipeCommand<String, Object>() {
        @Override
        public void provide(String element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ThrowCheckedException extends BaseCommand {
    @Command
    public String main() throws NamingException {
      throw new javax.naming.NamingException();
    }
  }

  public static class ThrowRuntimeException extends BaseCommand {
    @Command
    public String main() {
      throw new java.lang.SecurityException();
    }
  }

  public static class ThrowScriptException extends BaseCommand {
    @Command
    public String main() {
      throw new org.crsh.command.ScriptException();
    }
  }

  public static class ThrowGroovyScriptException extends CRaSHCommand {
    @Command
    public String main() throws groovy.util.ScriptException {
      throw new groovy.util.ScriptException();
    }
  }

  public static class ThrowError extends BaseCommand {
    @Command
    public String main() {
      throw new java.awt.AWTError("whatever");
    }
  }

  public static class CannotInstantiate extends BaseCommand {
    public CannotInstantiate() {
      throw new RuntimeException();
    }
    @Command
    public void main() {
    }
  }

  public static class Complete extends BaseCommand implements Completer {
    public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
      return Completion.builder(prefix).add("bar", true).build();
    }
    @Command
    public void main(@Argument(completer = Complete.class) String arg) {
    }
  }

  public static class CompleteWithSession extends BaseCommand implements Completer {
    public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
      Object juu = context.getSession().get("juu");
      Completion.Builder ret = Completion.builder(prefix);
      if (juu != null) {
        ret.add(juu.toString(), true);
      }
      return ret.build();
    }
    @Command
    public void main(@Argument(completer = CompleteWithSession.class) String arg) {
    }
  }

  public static class FailDuringOpen1 extends BaseCommand {

    public static void reset() {
      openCount.set(0);
      provideCound.set(0);
      flushCount.set(0);
      closeCount.set(0);
    }

    /** . */
    static final AtomicInteger openCount = new AtomicInteger();

    /** . */
    static final AtomicInteger provideCound = new AtomicInteger();

    /** . */
    static final AtomicInteger flushCount = new AtomicInteger();

    /** . */
    static final AtomicInteger closeCount = new AtomicInteger();

    @Command
    public org.crsh.command.PipeCommand<String, Object> main() {
      return new PipeCommand<String, Object>() {
        @Override
        public void open() throws ScriptException {
          openCount.incrementAndGet();
          throw new ScriptException();
        }
        @Override
        public void provide(String element) throws ScriptException, IOException {
          provideCound.incrementAndGet();
        }
        @Override
        public void flush() throws ScriptException, IOException {
          flushCount.incrementAndGet();
        }
        @Override
        public void close() throws ScriptException {
          closeCount.incrementAndGet();
        }
      };
    }
  }

  public static class FailDuringOpen2 extends BaseCommand {

    public static void reset() {
      openCount.set(0);
      provideCound.set(0);
      flushCount.set(0);
      closeCount.set(0);
    }

    /** . */
    static final AtomicInteger openCount = new AtomicInteger();

    /** . */
    static final AtomicInteger provideCound = new AtomicInteger();

    /** . */
    static final AtomicInteger flushCount = new AtomicInteger();

    /** . */
    static final AtomicInteger closeCount = new AtomicInteger();

    @Command
    public org.crsh.command.PipeCommand<String, Object> main() {
      return new PipeCommand<String, Object>() {
        @Override
        public void open() throws ScriptException {
          openCount.incrementAndGet();
        }
        @Override
        public void provide(String element) throws ScriptException, IOException {
          provideCound.incrementAndGet();
        }
        @Override
        public void flush() throws ScriptException, IOException {
          flushCount.incrementAndGet();
        }
        @Override
        public void close() throws ScriptException {
          closeCount.incrementAndGet();
        }
      };
    }
  }
}
