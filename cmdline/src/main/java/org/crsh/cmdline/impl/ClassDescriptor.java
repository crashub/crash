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
import org.crsh.cmdline.invocation.InvocationMatch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ClassDescriptor<T> extends CommandDescriptor<T> {

  /** . */
  private final Class<T> type;

  /** . */
  private final Map<String, MethodDescriptor<T>> methods;

  ClassDescriptor(Class<T> type, Map<String, MethodDescriptor<T>> methods, Description info) throws IntrospectionException {
    super(type.getSimpleName().toLowerCase(), info);

    //
    this.methods = methods;
    this.type = type;
  }

  @Override
  public InvocationMatch<T> createInvocationMatch() {
    return new ClassMatch<T>(this);
  }

  @Override
  protected void addParameter(ParameterDescriptor parameter) throws IntrospectionException {

    // Check we can add the option
    if (parameter instanceof OptionDescriptor) {
      OptionDescriptor option = (OptionDescriptor)parameter;
      Set<String> blah = new HashSet<String>();
      for (String optionName : option.getNames()) {
        blah.add((optionName.length() == 1 ? "-" : "--") + optionName);
      }
      for (MethodDescriptor<T> method : methods.values()) {
        Set<String> diff = new HashSet<String>(method.getOptionNames());
        diff.retainAll(blah);
        if (diff.size() > 0) {
          throw new IntrospectionException("Cannot add method " + method.getName() + " because it has common "
          + " options with its class: " + diff);
        }
      }
    }

    //
    super.addParameter(parameter);
  }

  @Override
  public CommandDescriptor<T> getOwner() {
    return null;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public Map<String, ? extends MethodDescriptor<T>> getSubordinates() {
    return methods;
  }

  @Override
  public OptionDescriptor findOption(String name) {
    return getOption(name);
  }

  public MethodDescriptor<T> getSubordinate(String name) {
    return methods.get(name);
  }
}
