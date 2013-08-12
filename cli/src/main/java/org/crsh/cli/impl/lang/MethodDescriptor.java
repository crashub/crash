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
import org.crsh.cli.impl.descriptor.CommandDescriptorImpl;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.SyntaxException;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.ParameterMatch;
import org.crsh.cli.impl.invocation.Resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

class MethodDescriptor<T> extends CommandDescriptorImpl<T> {

  /** . */
  private final ClassDescriptor<T> owner;

  /** . */
  private final Method method;

  /** . */
  private final int size;

  public MethodDescriptor(
    ClassDescriptor<T> owner,
    Method method,
    String name,
    Description info) throws IntrospectionException {
    super(name, info);

    //
    this.owner = owner;
    this.method = method;
    this.size = method.getParameterTypes().length;
  }

  /**
   * Returns the parameter descriptor for the specified method parameter index.
   *
   * @param index the parameter index
   * @return the parameter descriptor or null if none can be bound
   * @throws IndexOutOfBoundsException if the index is not valid
   */
  ParameterDescriptor getParameter(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Bad index value " + index);
    }
    for (ParameterDescriptor argument : getParameters()) {
      if (((MethodArgumentBinding)argument.getBinding()).getIndex() == index) {
        return argument;
      }
    }
    return null;
  }

  @Override
  protected void addParameter(ParameterDescriptor parameter) throws IntrospectionException, NullPointerException, IllegalArgumentException {
    super.addParameter(parameter);
  }

  @Override
  public CommandDescriptor<T> getOwner() {
    return owner;
  }

  @Override
  public Map<String, ? extends CommandDescriptorImpl<T>> getSubordinates() {
    return Collections.emptyMap();
  }

  @Override
  public CommandDescriptorImpl<T> getSubordinate(String name) {
    return null;
  }

  public Method getMethod() {
    return method;
  }

  @Override
  public Class<T> getType() {
    return owner.getType();
  }

  public CommandInvoker<T, ?> getInvoker(final InvocationMatch<T> match) {
    Class<?> type = method.getReturnType();
    return getInvoker2(match, type);
  }

  private <V> CommandInvoker<T, V> getInvoker2(final InvocationMatch<T> _match, final Class<V> returnType) {
    return new CommandInvoker<T, V>() {
      @Override
      public InvocationMatch<T> getMatch() {
        return _match;
      }
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
      public V invoke(Resolver resolver, T command) throws InvocationException, SyntaxException {

        //
        owner.configure(_match.owner(), command);

        // Prepare invocation
        Method m = getMethod();
        Class<?>[] parameterTypes = m.getParameterTypes();
        Object[] mArgs = new Object[parameterTypes.length];
        for (int i = 0;i < mArgs.length;i++) {
          ParameterDescriptor parameter = getParameter(i);

          //
          Class<?> parameterType = parameterTypes[i];

          Object v;
          if (parameter == null) {
            // Attempt to obtain from resolver
            v = resolver.resolve(parameterType);
          } else {
            ParameterMatch match = _match.getParameter(parameter);
            if (match != null) {
              v = match.computeValue();
            } else {
              v = null;
            }
          }

          //
          if (v == null) {
            if (parameterType.isPrimitive() || parameter.isRequired()) {
              if (parameter instanceof ArgumentDescriptor) {
                ArgumentDescriptor argument = (ArgumentDescriptor)parameter;
                throw new SyntaxException("Missing argument " + argument.getName());
              } else {
                OptionDescriptor option = (OptionDescriptor)parameter;
                throw new SyntaxException("Missing option " + option.getNames());
              }
            }
          }

          //
          mArgs[i] = v;
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
