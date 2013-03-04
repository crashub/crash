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

package org.crsh.cmdline.impl;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.SyntaxException;
import org.crsh.cmdline.invocation.InvocationException;
import org.crsh.cmdline.invocation.InvocationMatch;
import org.crsh.cmdline.invocation.CommandInvoker;
import org.crsh.cmdline.invocation.ParameterMatch;
import org.crsh.cmdline.invocation.Resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class MethodMatch<T> extends InvocationMatch<T> {

  /** . */
  private final MethodDescriptor<T> descriptor;

  /** . */
  private final ClassMatch<T> owner;

  MethodMatch(ClassMatch<T> owner, MethodDescriptor<T> descriptor) {
    super(descriptor);

    //
    this.descriptor = descriptor;
    this.owner = owner;
  }

  @Override
  public InvocationMatch<T> owner() {
    return owner;
  }

  @Override
  public InvocationMatch<T> subordinate(String name) {
    return null;
  }

  @Override
  public void configure(T command) throws InvocationException, SyntaxException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CommandInvoker<T> getInvoker() {
    return new CommandInvoker<T>() {
      @Override
      public Class<?> getReturnType() {
        return descriptor.getMethod().getReturnType();
      }

      @Override
      public Type getGenericReturnType() {
        return descriptor.getMethod().getGenericReturnType();
      }

      @Override
      public Class<?>[] getParameterTypes() {
        return descriptor.getMethod().getParameterTypes();
      }

      @Override
      public Type[] getGenericParameterTypes() {
        return descriptor.getMethod().getGenericParameterTypes();
      }

      @Override
      public Object invoke(Resolver resolver, T command) throws InvocationException, SyntaxException {

        //
        owner.configure(command);

        // Prepare invocation
        Method m = descriptor.getMethod();
        Class<?>[] parameterTypes = m.getParameterTypes();
        Object[] mArgs = new Object[parameterTypes.length];
        for (int i = 0;i < mArgs.length;i++) {
          ParameterDescriptor parameter = descriptor.getParameter(i);

          //
          Class<?> parameterType = parameterTypes[i];

          Object v;
          if (parameter == null) {
            // Attempt to obtain from resolver
            v = resolver.resolve(parameterType);
          } else {
            ParameterMatch match = getParameter(parameter);
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
          return m.invoke(command, mArgs);
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
