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

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import org.crsh.lang.impl.groovy.Helper;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.lang.impl.groovy.closure.PipeLineClosure;
import org.crsh.text.RenderPrintWriter;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;

public abstract class GroovyScriptCommand extends Script {

  /** . */
  private LinkedList<InvocationContext<?>> stack;

  /** The current context. */
  protected InvocationContext context;

  /** The current output. */
  protected RenderPrintWriter out;

  protected GroovyScriptCommand() {
    this.stack = null;
  }

  public final void pushContext(InvocationContext<?> context) throws NullPointerException {
    if (context == null) {
      throw new NullPointerException();
    }

    //
    if (stack == null) {
      stack = new LinkedList<InvocationContext<?>>();
    }

    // Save current context (is null the first time)
    stack.addLast(this.context);

    // Set new context
    this.context = context;
    this.out = context.getWriter();
  }

  public final InvocationContext<?> popContext() {
    if (stack == null || stack.isEmpty()) {
      throw new IllegalStateException("Cannot pop a context anymore from the stack");
    }
    InvocationContext context = this.context;
    this.context = stack.removeLast();
    this.out = this.context != null ? this.context.getWriter() : null;
    return context;
  }

  public final void execute(String s) throws Exception {
    InvocationContext<?> context = peekContext();
    try {
      CommandInvoker invoker = context.resolve(s);
      invoker.invoke(context);
    }
    catch (CommandException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) {
        throw (Exception)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new UndeclaredThrowableException(cause);
      }
    }
  }

  public final InvocationContext<?> peekContext() {
    return (InvocationContext<?>)context;
  }

  @Override
  public final Object invokeMethod(String name, Object args) {

    //
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException missing) {
      return Helper.invokeMethod(context, name, args, missing);
    }
  }

  @Override
  public final Object getProperty(String property) {
    if ("out".equals(property)) {
      return ((InvocationContext<?>)context).getWriter();
    } else if ("context".equals(property)) {
      return context;
    } else {
      PipeLineClosure ret = Helper.resolveProperty(context, property);
      if (ret != null) {
        return ret;
      }
      try {
        return super.getProperty(property);
      }
      catch (MissingPropertyException e) {
        return null;
      }
    }
  }
}
