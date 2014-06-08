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

package test.command;

import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseCommand;
import org.crsh.command.Pipe;
import org.crsh.command.ScriptException;
import org.crsh.groovy.GroovyCommand;
import org.crsh.text.CLS;
import org.crsh.text.Screenable;
import org.crsh.text.ScreenContext;
import org.crsh.text.Style;
import test.text.Value;

import javax.naming.NamingException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Commands {

  /** . */
  public static final ArrayList<Object> list = new ArrayList<Object>();

  public static class Noop extends BaseCommand {
    @Command
    public Pipe<Object, Object> main() throws IOException {
      return new Pipe<Object, Object>() {
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
    public void main(org.crsh.command.InvocationContext<Value> context) throws Exception {
      context.provide(new Value("abc"));
    }
  }

  public static class ProduceString extends BaseCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<String> context) throws Exception {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class ConsumeCharSequence extends BaseCommand {
    @Command
    public Pipe<CharSequence, Object> main() {
      return new Pipe<CharSequence, Object>() {
        @Override
        public void provide(CharSequence element) throws ScriptException, IOException {
          list.add(element.toString());
        }
      };
    }
  }

  public static class ConsumeString extends BaseCommand {
    @Command
    public Pipe<String, Object> main() {
      return new Pipe<String, Object>() {
        @Override
        public void provide(String element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class Id extends BaseCommand {
    @Command
    public Pipe<Object, Object> main() {
      return new Pipe<Object, Object>() {
        @Override
        public void provide(Object element) throws Exception {
          context.provide(element);
        }
      };
    }
  }

  public static class Count extends BaseCommand {
    @Command
    public Pipe<Object, Integer> main() {
      return new Pipe<Object, Integer>() {
        int count = 0;
        @Override
        public void provide(Object element) throws ScriptException, IOException {
          System.out.println("getClass().getName() = " + getClass().getName());
          count++;
        }
        @Override
        public void close() throws Exception {
          context.provide(count);
        }
      };
    }
  }

  public static class Buffer extends BaseCommand {
    @Command
    public Pipe<String, String> main() {
      return new Pipe<String, String>() {
        List<String> buffer = new ArrayList<String>();
        @Override
        public void provide(String element) throws ScriptException, IOException {
          buffer.add(element);
        }
        @Override
        public void flush() throws IOException {
          for (String s : buffer) {
            try {
              context.provide(s);
            }
            catch (Exception e) {
              throw new UndeclaredThrowableException(e);
            }
          }
          buffer.clear();
          super.flush();
        }
      };
    }
  }

  public static class Filter extends BaseCommand {
    @Command
    public Pipe<String, String> main() {
      return new Pipe<String, String>() {
        @Override
        public void provide(String element) throws Exception {
          context.provide(element);
        }
      };
    }
  }

  public static class ProduceInteger extends BaseCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<Integer> context) throws Exception {
      context.provide(3);
    }
  }

  public static class ReturnInteger extends BaseCommand {
    @Command
    public Integer main() {
      return 3;
    }
  }

  public static class ConsumeInteger extends BaseCommand {
    @Command
    public Pipe<Integer, Object> main() {
      return new Pipe<Integer, Object>() {
        @Override
        public void provide(Integer element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ConsumeBoolean extends BaseCommand {
    @Command
    public Pipe<Boolean, Object> main() {
      return new Pipe<Boolean, Object>() {
        @Override
        public void provide(Boolean element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ConsumeChunk extends BaseCommand {

    static class ConsumePipe extends Pipe<CharSequence, Object> implements ScreenContext {
      public int getWidth() { return context.getWidth(); }
      public int getHeight() { return context.getHeight(); }
      public Appendable append(char c) throws IOException { list.add("" + c); return this; }
      public Appendable append(CharSequence s) throws IOException { list.add(s); return this; }
      public Appendable append(CharSequence csq, int start, int end) throws IOException { list.add(csq.subSequence(start, end)); return this; }
      public Screenable append(Style style) throws IOException { list.add(style); return this; }
      public Screenable cls() throws IOException { list.add(CLS.INSTANCE); return this; }
      public void provide(CharSequence element) throws ScriptException, IOException { list.add(element); }
    }

    @Command
    public Pipe<CharSequence, Object> main() {
      return new ConsumePipe();
    }
  }

  public static class ConsumeObject extends BaseCommand {
    @Command
    public Pipe<Object, Object> main() {
      return new Pipe<Object, Object>() {
        @Override
        public void provide(Object element) throws ScriptException, IOException {
          list.add(element);
        }
      };
    }
  }

  public static class ParameterizedConsumeToList extends BaseCommand {
    @Command
    public Pipe<String, Object> main(final @Option(names={"opt"}) String opt, @Argument List<String> args) {
      if (args != null) {
        for (String arg : args) {
          list.add((opt != null ? opt : "") + arg);
        }
      }
      return new Pipe<String, Object>() {
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
    public Pipe<Void, Object> main() {
      return new Pipe<Void, Object>() {
        @Override
        public void close() throws ScriptException {
          closed.incrementAndGet();
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
    public void compound(org.crsh.command.InvocationContext<String> context) throws Exception {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class CompoundConsumeString extends BaseCommand {
    @Command
    public Pipe<String, Object> compound() {
      return new Pipe<String, Object>() {
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

  public static class ThrowGroovyScriptException extends GroovyCommand {
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

  public static class SubordinateProduceInteger extends BaseCommand {
    @Command
    public void sub(org.crsh.command.InvocationContext<Integer> context) throws Exception {
      context.provide(3);
    }
  }
}
