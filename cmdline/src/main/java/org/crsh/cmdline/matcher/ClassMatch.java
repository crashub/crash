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

package org.crsh.cmdline.matcher;

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.ParameterDescriptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassMatch<T> extends CommandMatch<T, ClassDescriptor<T>, ClassFieldBinding> {

  /** . */
  private final ClassDescriptor<T> descriptor;

  public ClassMatch(
    ClassDescriptor<T> descriptor,
    List<OptionMatch<ClassFieldBinding>> optionMatches,
    List<ArgumentMatch<ClassFieldBinding>> argumentMatches,
    String rest) {
    super(optionMatches, argumentMatches, rest);

    //
    this.descriptor = descriptor;
  }

  @Override
  public ClassDescriptor<T> getDescriptor() {
    return descriptor;
  }

  @Override
  public void printMan(Appendable writer) throws IOException {
    descriptor.printMan(writer);
  }

  @Override
  public void printUsage(Appendable writer) throws IOException {
    descriptor.printUsage(writer);
  }

  @Override
  public Set<ParameterDescriptor<?>> getParameters() {
    Set<ParameterDescriptor<?>> unused = new HashSet<ParameterDescriptor<?>>();
    unused.addAll(descriptor.getArguments());
    unused.addAll(descriptor.getOptions());
    return unused;
  }

  @Override
  public List<ParameterMatch<?, ?>> getParameterMatches() {
    List<ParameterMatch<?, ?>> matches = new ArrayList<ParameterMatch<?, ?>>();
    matches.addAll(getOptionMatches());
    matches.addAll(getArgumentMatches());
    return matches;
  }

  @Override
  protected Object doInvoke(InvocationContext context, T command, Map<ParameterDescriptor<?>, Object> values) throws CmdInvocationException, CmdSyntaxException {
    for (ParameterDescriptor<ClassFieldBinding> parameter : descriptor.getParameters()) {
      Object value = values.get(parameter);

      //
      if (value == null) {
        if (parameter.isRequired()) {
          if (parameter instanceof ArgumentDescriptor) {
            ArgumentDescriptor<?> argument = (ArgumentDescriptor<?>)parameter;
            throw new CmdSyntaxException("Missing argument " + argument.getName());
          } else {
            OptionDescriptor<?> option = (OptionDescriptor<?>)parameter;
            throw new CmdSyntaxException("Missing option " + option.getNames());
          }
        }
      } else {
        Field f = parameter.getBinding().getField();
        try {
          f.setAccessible(true);
          f.set(command, value);
        }
        catch (Exception e) {
          throw new CmdInvocationException(e.getMessage(), e);
        }
      }
    }

    //
    return null;
  }
}
