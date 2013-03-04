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
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.invocation.InvocationException;
import org.crsh.cmdline.invocation.InvocationMatch;
import org.crsh.cmdline.invocation.CommandInvoker;
import org.crsh.cmdline.invocation.ParameterMatch;

import java.lang.reflect.Field;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ClassMatch<T> extends InvocationMatch<T> {

  /** . */
  private final ClassDescriptor<T> descriptor;

  ClassMatch(ClassDescriptor<T> descriptor) {
    super(descriptor);

    //
    this.descriptor = descriptor;
  }

  @Override
  public InvocationMatch<T> owner() {
    return null;
  }

  @Override
  public InvocationMatch<T> subordinate(String name) {
    MethodDescriptor<T> subordinate = descriptor.getSubordinate(name);
    if (subordinate != null) {
      return new MethodMatch<T>(this, subordinate);
    } else {
      return null;
    }
  }

  @Override
  public void configure(T command) throws InvocationException, SyntaxException {
    for (ParameterDescriptor parameter : descriptor.getParameters()) {
      ParameterMatch match = getParameter(parameter);
      if (match == null) {
        if (parameter.isRequired()) {
          if (parameter instanceof ArgumentDescriptor) {
            ArgumentDescriptor argument = (ArgumentDescriptor)parameter;
            throw new SyntaxException("Missing argument " + argument.getName());
          } else {
            OptionDescriptor option = (OptionDescriptor)parameter;
            throw new SyntaxException("Missing option " + option.getNames());
          }
        }
      } else {
        Object value = match.computeValue();
        Field f = ((ClassFieldBinding)parameter.getBinding()).getField();
        try {
          f.setAccessible(true);
          f.set(command, value);
        }
        catch (Exception e) {
          throw new InvocationException(e.getMessage(), e);
        }
      }
    }
  }

  @Override
  public CommandInvoker<T> getInvoker() {
    return null;
  }
}
