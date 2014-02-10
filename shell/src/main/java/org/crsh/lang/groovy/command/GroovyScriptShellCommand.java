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
package org.crsh.lang.groovy.command;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandCreationException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.DescriptionFormat;
import org.crsh.command.InvocationContextImpl;
import org.crsh.command.RuntimeContext;
import org.crsh.command.ShellCommand;
import org.crsh.shell.ErrorType;
import org.crsh.text.RenderPrintWriter;
import org.crsh.util.Utils;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;

/** @author Julien Viet */
public class GroovyScriptShellCommand<C extends GroovyScriptCommand> implements ShellCommand {

  /** . */
  private final Class<C> clazz;

  public GroovyScriptShellCommand(Class<C> clazz) {
    this.clazz = clazz;
  }

  private C createCommand() throws CommandCreationException {
    C command;
    try {
      command = clazz.newInstance();
    }
    catch (Exception e) {
      String name = clazz.getSimpleName();
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not create command " + name + " instance", e);
    }
    return command;
  }

  public final CompletionMatch complete(RuntimeContext context, String line) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }

  public final String describe(String line, DescriptionFormat mode) {
    return null;
  }

  public final CommandInvoker<?, ?> resolveInvoker(String line) throws CommandCreationException {
    List<String> chunks = Utils.chunks(line);
    String[] args = chunks.toArray(new String[chunks.size()]);
    return getInvoker(args);
  }

  public final CommandInvoker<?, ?> resolveInvoker(Map<String, ?> options, String subordinate, Map<String, ?> subordinateOptions, List<?> arguments) throws CommandCreationException {
    String[] tmp = new String[arguments.size()];
    for (int i = 0;i < tmp.length;i++) {
      tmp[i] = arguments.get(i).toString();
    }
    return getInvoker(tmp);
  }

  private CommandInvoker<Object, Object> getInvoker(final String[] args) throws CommandCreationException {
    final C command = createCommand();
    return new CommandInvoker<Object, Object>() {

      public final Class<Object> getProducedType() {
        return Object.class;
      }

      public final Class<Object> getConsumedType() {
        return Object.class;
      }

      public void open(CommandContext<? super Object> consumer) {
        // Set up current binding
        Binding binding = new Binding(consumer.getSession());

        // Set the args on the script
        binding.setProperty("args", args);

        //
        command.setBinding(binding);

        //
        command.pushContext(new InvocationContextImpl<Object>((CommandContext<Object>)consumer));

        //
        try {
          //
          Object res = command.run();

          // Evaluate the closure
          if (res instanceof Closure) {
            Closure closure = (Closure)res;
            res = closure.call(args);
          }

          //
          if (res != null) {
            RenderPrintWriter writer = command.peekContext().getWriter();
            if (writer.isEmpty()) {
              writer.print(res);
            }
          }
        }
        catch (Exception t) {
          throw GroovyCommand.unwrap(t);
        }
      }

      public void provide(Object element) throws IOException {
        // Should never be called
      }

      public void flush() throws IOException {
        command.peekContext().flush();
      }

      public void close() throws IOException, UndeclaredThrowableException {
        command.popContext();
      }
    };
  }


}
