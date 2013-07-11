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

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.PipeCommandProxy;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.io.Consumer;
import org.crsh.util.Safe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ClassDispatcher extends CommandClosure {

  /** . */
  final Object owner;

  /** . */
  final ShellCommand command;

  ClassDispatcher(ShellCommand command, Object owner) {
    super(new Object());

    //
    this.command = command;
    this.owner = owner;
  }

  @Override
  public Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return new MethodDispatcher(this, property);
    }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      return dispatch(name, unwrapArgs(args));
    }
  }

  /**
   * Closure invocation.
   *
   * @param arguments the closure arguments
   */
  public Object call(Object[] arguments) {
    return dispatch("", arguments);
  }

  Object dispatch(String methodName, Object[] arguments) {
    PipeCommandProxy pipe = resolvePipe(methodName, arguments, false);

    //
    try {
      pipe.fire();
      return null;
    }
    catch (ScriptException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        throw new InvokerInvocationException(cause);
      } else {
        throw e;
      }
    }
    finally {
      Safe.close(pipe);
    }
  }

  private PipeCommandProxy<?, Object> resolvePipe(String name, Object[] args, boolean piped) {
    final Closure closure;
    int to = args.length;
    if (to > 0 && args[to - 1] instanceof Closure) {
      closure = (Closure)args[--to];
    } else {
      closure = null;
    }

    //
    Map<String, Object> invokerOptions = this.options != null ? this.options : Collections.<String, Object>emptyMap();
    List<Object> invokerArgs = this.args != null ? this.args : Collections.emptyList();

    //
    if (to > 0) {
      Object first = args[0];
      int from;
      if (first instanceof Map<?, ?>) {
        from = 1;
        Map<?, ?> options = (Map<?, ?>)first;
        if (options.size() > 0) {
          invokerOptions = new HashMap<String, Object>(invokerOptions);
          for (Map.Entry<?, ?> option : options.entrySet()) {
            String optionName = option.getKey().toString();
            Object optionValue = option.getValue();
            invokerOptions.put(optionName, optionValue);
          }
        }
      } else {
        from = 0;
      }

      if (from < to) {
        invokerArgs = new ArrayList<Object>(invokerArgs);
        while (from < to) {
          Object o = args[from++];
          if (o != null) {
            invokerArgs.add(o);
          }
        }
      }
    }

    //
    CommandInvoker<Void, Void> invoker = (CommandInvoker<Void, Void>)command.resolveInvoker(name, invokerOptions, invokerArgs);

    //
    InvocationContext context;
    if (owner instanceof CRaSHCommand) {
      context = ((CRaSHCommand)owner).peekContext();
    } else if (owner instanceof GroovyScriptCommand) {
      context = (InvocationContext)((GroovyScriptCommand)owner).peekContext();
    } else {
      throw new UnsupportedOperationException("todo");
    }

    //
    Consumer producer;
    if (closure != null) {
      CommandInvoker producerPipe;
      if (closure instanceof MethodDispatcher) {
        MethodDispatcher commandClosure = (MethodDispatcher)closure;
        producerPipe = commandClosure.dispatcher.resolvePipe(commandClosure.name, new Object[0], true);
      } else if (closure instanceof ClassDispatcher) {
        ClassDispatcher dispatcherClosure = (ClassDispatcher)closure;
        producerPipe = dispatcherClosure.resolvePipe(name, new Object[0], true);
      } else {

        // That's the type we cast to
        Class[] pt = closure.getParameterTypes();
        final Class type;
        if (pt.length > 0) {
          type = pt[0];
        } else {
          type = Void.class;
        }

        //
        producerPipe = new CommandInvoker<Object, Void>() {
          public Class<Void> getProducedType() {
            return Void.class;
          }
          public Class<Object> getConsumedType() {
            return type;
          }
          public void open(CommandContext<Void> consumer) {
          }
          public void close() {
          }
          public void provide(Object element) throws IOException {
            if (type.isInstance(element)) {
              closure.call(element);
            }
          }
          public void flush() throws IOException {
          }
        };
      }
      producer = producerPipe;
    } else {
      producer = context;
    }

    //
    InnerInvocationContext inner = new InnerInvocationContext(context, producer, piped);
    return new PipeCommandProxy(inner, invoker, producer);
  }
}
