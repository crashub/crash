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

import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Description;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.invocation.InvocationMatch;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

class MethodDescriptor<T> extends CommandDescriptor<T> {

  /** . */
  private final CommandDescriptor<T> owner;

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

  @Override
  public InvocationMatch<T> createInvocationMatch() {
    throw new UnsupportedOperationException("Not yet implemented");
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
  public Map<String, ? extends CommandDescriptor<T>> getSubordinates() {
    return Collections.emptyMap();
  }

  @Override
  public CommandDescriptor<T> getSubordinate(String name) {
    return null;
  }

  public Method getMethod() {
    return method;
  }

  @Override
  public Class<T> getType() {
    return owner.getType();
  }

  @Override
  public OptionDescriptor findOption(String name) {
    OptionDescriptor option = getOption(name);
    if (option == null) {
      option = owner.findOption(name);
    }
    return option;
  }
}
