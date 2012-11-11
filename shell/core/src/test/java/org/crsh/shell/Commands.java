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

import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.spi.Completer;
import org.crsh.cmdline.spi.Completion;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.PipeCommand;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commands {

  /** . */
  public static final ArrayList<Object> list = new ArrayList<Object>();

  public static class Noop extends CRaSHCommand {
    @Command
    public PipeCommand<Object, Object> main() throws IOException {
      return new PipeCommand<Object, Object>() {
      };
    }
  }

  public static class ProduceValue extends CRaSHCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<Value> context) throws IOException {
      context.provide(new Value("abc"));
    }
  }

  public static class ProduceString extends CRaSHCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<String> context) throws IOException {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class ConsumeString extends CRaSHCommand {
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

  public static class Buffer extends CRaSHCommand {
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

  public static class Filter extends CRaSHCommand {
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

  public static class ProduceInteger extends CRaSHCommand {
    @Command
    public void main(org.crsh.command.InvocationContext<Integer> context) throws IOException {
      context.provide(3);
    }
  }

  public static class ConsumeInteger extends CRaSHCommand {
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

  public static class ConsumeBoolean extends CRaSHCommand {
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

  public static class ConsumeChunk extends CRaSHCommand {
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

  public static class ParameterizedConsumeToList extends CRaSHCommand {
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

  public static class IsPiped extends CRaSHCommand {
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

  public static class Compound extends CRaSHCommand {
    @Command
    public String compound() {
      return "bar";
    }
  }

  public static class CompoundProduceString extends CRaSHCommand {
    @Command
    public void compound(org.crsh.command.InvocationContext<String> context) throws IOException {
      context.provide("foo");
      context.provide("bar");
    }
  }

  public static class CompoundConsumeString extends CRaSHCommand {
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

  public static class ThrowCheckedException extends CRaSHCommand {
    @Command
    public String main() throws NamingException {
      throw new javax.naming.NamingException();
    }
  }

  public static class ThrowRuntimeException extends CRaSHCommand {
    @Command
    public String main() {
      throw new java.lang.SecurityException();
    }
  }

  public static class ThrowScriptException extends CRaSHCommand {
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

  public static class ThrowError extends CRaSHCommand {
    @Command
    public String main() {
      throw new java.awt.AWTError("whatever");
    }
  }

  public static class CannotInstantiate extends CRaSHCommand {
    public CannotInstantiate() {
      throw new RuntimeException();
    }
    @Command
    public void main() {
    }
  }

  public static class Complete extends CRaSHCommand implements Completer {
    public Completion complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
      return org.crsh.cmdline.spi.Completion.builder(prefix).add("bar", true).build();
    }
    @Command
    public void main(@Argument String arg) {
    }
  }

  public static class CompleteWithSession extends CRaSHCommand implements Completer {
    public Completion complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
      Object juu = context.getSession().get("juu");
      Completion.Builder ret = Completion.builder(prefix);
      if (juu != null) {
        ret.add(juu.toString(), true);
      }
      return ret.build();
    }
    @Command
    public void main(@Argument String arg) {
    }
  }
}
