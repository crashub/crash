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

package org.crsh.command;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.shell.impl.command.CRaSH;

import java.util.LinkedList;

public abstract class GroovyCommand extends GroovyObjectSupport {

  /** . */
  private LinkedList<InvocationContext<?>> stack;

  protected abstract CommandContext getContext();

  public final void pushContext(InvocationContext<?> context) {
    if (stack == null) {
      stack = new LinkedList<InvocationContext<?>>();
    }
    stack.addLast(context);
  }

  public final InvocationContext<?> popContext() {
    if (stack == null || stack.isEmpty()) {
      throw new IllegalStateException("Cannot pop a context anymore from the stack");
    }
    return stack.removeLast();
  }

  public final InvocationContext<?> peekContext() {
    return stack == null || stack.isEmpty() ? (InvocationContext<?>)getContext() : stack.getLast();
  }

  @Override
  public final Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {

      //
      CommandContext context = getContext();

      //
      if (context instanceof InvocationContext) {
        InvocationContext invocationContext = (InvocationContext)context;
        CRaSH crash = (CRaSH)context.getSession().get("crash");
        if (crash != null) {
          ShellCommand cmd;
          try {
            cmd = crash.getCommand(name);
          }
          catch (NoSuchCommandException ce) {
            throw new InvokerInvocationException(ce);
          }
          if (cmd != null) {
            InvocationContext outter;
            if (stack != null && stack.size() > 0) {
              outter = stack.getLast();
            } else {
              outter = invocationContext;
            }
            return new CommandDispatcher(cmd, outter).dispatch("", args);
          }
        }
      }

      //
      Object o = context.getSession().get(name);
      if (o instanceof Closure) {
        Closure closure = (Closure)o;
        if (args instanceof Object[]) {
          Object[] array = (Object[])args;
          if (array.length == 0) {
            return closure.call();
          } else {
            return closure.call(array);
          }
        } else {
          return closure.call(args);
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public final Object getProperty(String property) {
    CommandContext context = getContext();
    if ("out".equals(property)) {
      if (context instanceof InvocationContext<?>) {
        return ((InvocationContext<?>)context).getWriter();
      } else {
        return null;
      }
    } else if ("context".equals(property)) {
      return context;
    } else {
      if (context instanceof InvocationContext<?>) {
        CRaSH crash = (CRaSH)context.getSession().get("crash");
        if (crash != null) {
          try {
            ShellCommand cmd = crash.getCommand(property);
            if (cmd != null) {
              InvocationContext outter;
              if (stack != null && stack.size() > 0) {
                outter = stack.getLast();
              } else {
                outter = (InvocationContext)context;
              }
              return new CommandDispatcher(cmd, outter);
            }
          } catch (NoSuchCommandException e) {
            throw new InvokerInvocationException(e);
          }
        }
      }

      //
      try {
        return super.getProperty(property);
      }
      catch (MissingPropertyException e) {
        return context.getSession().get(property);
      }
    }
  }

  @Override
  public final void setProperty(String property, Object newValue) {
    if ("out".equals(property)) {
      throw new IllegalArgumentException("Cannot write out");
    }
    try {
      super.setProperty(property, newValue);
    }
    catch (MissingPropertyException e) {
      CommandContext context = getContext();
      context.getSession().put(property, newValue);
    }
  }
}
