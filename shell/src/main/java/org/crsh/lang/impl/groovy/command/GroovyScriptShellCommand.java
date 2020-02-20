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
package org.crsh.lang.impl.groovy.command;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.spi.Completer;
import org.crsh.command.CommandContext;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.groovy.GroovyCommand;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.lang.impl.groovy.ast.ScriptLastStatementTransformer;
import org.crsh.shell.impl.command.spi.CommandMatch;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.command.RuntimeContext;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.List;

/** @author Julien Viet */
public class GroovyScriptShellCommand<T extends GroovyScriptCommand> extends Command<Instance<T>> {

  /** . */
  private final Class<T> clazz;

  /** . */
  private final boolean hasExplicitReturn;

  /** . */
  private final CommandDescriptor<Instance<T>> descriptor;

  public GroovyScriptShellCommand(Class<T> clazz) throws IntrospectionException {

    //
    CommandFactory factory = new CommandFactory(getClass().getClassLoader());

    boolean hasExplicitReturn;
    try {
      clazz.getDeclaredField(ScriptLastStatementTransformer.FIELD_NAME);
      hasExplicitReturn = true;
    }
    catch (NoSuchFieldException e) {
      hasExplicitReturn = false;
    }

    //
    this.clazz = clazz;
    this.descriptor = HelpDescriptor.create(factory.create(clazz));
    this.hasExplicitReturn = hasExplicitReturn;
  }

  @Override
  public CommandDescriptor<Instance<T>> getDescriptor() {
    return descriptor;
  }

  @Override
  protected CommandMatch<?, ?> resolve(final InvocationMatch<Instance<T>> match) {
    return new CommandMatch<Void, Object>() {
      @Override
      public CommandInvoker<Void, Object> getInvoker() throws CommandException {
        List<String> chunks = Utils.chunks(match.getRest());
        String[] args = chunks.toArray(new String[chunks.size()]);
        return GroovyScriptShellCommand.this.getInvoker(args);
      }

      @Override
      public Class<Object> getProducedType() {
        return Object.class;
      }

      @Override
      public Class<Void> getConsumedType() {
        return Void.class;
      }
    };
  }

  private T createCommand() throws CommandException {
    T command;
    try {
      command = clazz.newInstance();
    }
    catch (Exception e) {
      String name = clazz.getSimpleName();
      throw new CommandException(ErrorKind.INTERNAL, "Could not create command " + name + " instance", e);
    }
    return command;
  }

  @Override
  protected Completer getCompleter(RuntimeContext context) throws CommandException {
    return null;
  }

  private CommandInvoker<Void, Object> getInvoker(final String[] args) throws CommandException {
    final T command = createCommand();
    return new CommandInvoker<Void, Object>() {

      /** . */
      private org.crsh.command.InvocationContext<Object> context;

      public final Class<Object> getProducedType() {
        return Object.class;
      }

      public final Class<Void> getConsumedType() {
        return Void.class;
      }

      public void open(CommandContext<? super Object> consumer) throws IOException, CommandException {

        // Set the context
        context = new InvocationContextImpl<Object>((CommandContext<Object>)consumer, ShellSafetyFactory.getCurrentThreadShellSafety());

        // Set up current binding
        Binding binding = new Binding(consumer.getSession());

        // Set the args on the script
        binding.setProperty("args", args);

        //
        command.setBinding(binding);


        //
        command.pushContext(context);

        //
        try {
          //
          Object ret = command.run();

          // Evaluate the closure
          if (ret instanceof Closure) {
            Closure closure = (Closure)ret;
            ret = closure.call(args);
          }

          //
          if (ret != null) {
            if (hasExplicitReturn) {
              context.provide(ret);
            }
          }
        }
        catch (Exception t) {
          throw new CommandException(ErrorKind.EVALUATION, GroovyCommand.unwrap(t));
        }
      }

      public void provide(Void element) {
        // Should never be called
      }

      public void flush() throws IOException {
        context.flush();
      }

      public void close() throws IOException {
        context = null;
        command.popContext();
      }
    };
  }


}
