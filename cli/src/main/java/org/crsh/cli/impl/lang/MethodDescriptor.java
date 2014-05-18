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

package org.crsh.cli.impl.lang;

import org.crsh.cli.descriptor.ArgumentDescriptor;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.ParameterMatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

class MethodDescriptor<T> extends ObjectCommandDescriptor<T> {

  /** . */
  private final ClassDescriptor<T> owner;

  /** . */
  private final Method method;

  public MethodDescriptor(
    ClassDescriptor<T> owner,
    Method method,
    String name,
    Description info) throws IntrospectionException {
    super(name, info);

    //
    this.owner = owner;
    this.method = method;
  }

  @Override
  protected void addParameter(ParameterDescriptor parameter) throws IntrospectionException, NullPointerException, IllegalArgumentException {
    super.addParameter(parameter);
  }

  @Override
  public CommandDescriptor<Instance<T>> getOwner() {
    return owner;
  }

  @Override
  public Map<String, ? extends CommandDescriptor<Instance<T>>> getSubordinates() {
    return Collections.emptyMap();
  }

  public Method getMethod() {
    return method;
  }

  @Override
  public CommandInvoker<Instance<T>, ?> getInvoker(InvocationMatch<Instance<T>> match) {
    Class<?> type = method.getReturnType();
    return getInvoker2(match, type);
  }

  static void bind(InvocationMatch<?> match, Iterable<ParameterDescriptor> parameters, Object target, Object[] args) throws SyntaxException, InvocationException {
    for (ParameterDescriptor parameter : parameters) {
      ParameterMatch parameterMatch = match.getParameter(parameter);
      Object value = parameterMatch != null ? parameterMatch.computeValue() : null;
      if (value == null) {
        if (parameter.getDeclaredType().isPrimitive() || parameter.isRequired()) {
          if (parameter instanceof ArgumentDescriptor) {
            ArgumentDescriptor argument = (ArgumentDescriptor)parameter;
            throw new SyntaxException("Missing argument " + argument.getName());
          } else {
            OptionDescriptor option = (OptionDescriptor)parameter;
            throw new SyntaxException("Missing option " + option.getNames());
          }
        }
      } else {
        ((Binding)parameter).set(target, args, value);
      }
    }
  }

  private <V> ObjectCommandInvoker<T, V> getInvoker2(final InvocationMatch<Instance<T>> match, final Class<V> returnType) {
    return new ObjectCommandInvoker<T, V>(match) {
      @Override
      public Class<V> getReturnType() {
        return returnType;
      }
      @Override
      public Type getGenericReturnType() {
        return getMethod().getGenericReturnType();
      }
      @Override
      public Class<?>[] getParameterTypes() {
        return getMethod().getParameterTypes();
      }
      @Override
      public Type[] getGenericParameterTypes() {
        return getMethod().getGenericParameterTypes();
      }
      @Override
      public V invoke(Instance<T> commandInstance) throws InvocationException, SyntaxException {

        //
        T command = null;
        try {
          command = commandInstance.get();
        }
        catch (Exception e) {
          throw new InvocationException(e);
        }

        //
        if (owner != null) {
          bind(match.owner(), owner.getParameters(), command, Util.EMPTY_ARGS);
        }

        // Prepare invocation
        Method m = getMethod();
        Class<?>[] parameterTypes = m.getParameterTypes();
        Object[] mArgs = new Object[parameterTypes.length];

        // Bind method parameter first
        bind(match, getParameters(), command, mArgs);

        // Fill missing contextual parameters and make primitive check
        for (int i = 0;i < mArgs.length;i++) {
          Class<?> parameterType = parameterTypes[i];
          if (mArgs[i] == null) {
            Object v = commandInstance.resolve(parameterType);
            if (v != null) {
              mArgs[i] = v;
            }
          }
          if (mArgs[i] == null && parameterType.isPrimitive()) {
            throw new SyntaxException("Method argument at position " + i + " of " + m + " is missing");
          }
        }

        // Perform method invocation
        try {
          Object ret = m.invoke(command, mArgs);
          return returnType.cast(ret);
        }
        catch (InvocationTargetException e) {
          Throwable t = e.getTargetException();
          if (t instanceof Error) {
            throw (Error)t;
          } else {
            throw new InvocationException(t);
          }
        }
        catch (IllegalAccessException t) {
          throw new InvocationException(t);
        }
      }
    };
  }
}
