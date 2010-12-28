/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterDescriptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
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
  public Object invoke(InvocationContext context, T command) throws CmdLineException {
    List<ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding>> used = new ArrayList<ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding>>();
    Set<ParameterDescriptor<?>> unused = new HashSet<ParameterDescriptor<?>>();
    unused.addAll(descriptor.getArguments());
    unused.addAll(descriptor.getOptions());

    //
    for (OptionMatch<ClassFieldBinding> optionMatch : getOptionMatches()) {
      if (!unused.remove(optionMatch.getParameter())) {
        throw new CmdSyntaxException();
      }
      used.add(optionMatch);
    }

    //
    for (ArgumentMatch<ClassFieldBinding> argumentMatch : getArgumentMatches()) {
      if (!unused.remove(argumentMatch.getParameter())) {
        throw new CmdSyntaxException();
      }
      used.add(argumentMatch);
    }

    // Should be better with required / non required
    for (ParameterDescriptor<?> nonSatisfied : unused) {
      if (!nonSatisfied.isRequired()) {
        // Ok
      } else {
        throw new CmdSyntaxException("Non satisfied " + nonSatisfied);
      }
    }

    //
    for (ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding> parameterMatch : used) {

      ParameterDescriptor<ClassFieldBinding> parameter = parameterMatch.getParameter();
      ClassFieldBinding cf = parameter.getBinding();
      Field f = cf.getField();
      List<String> values = parameterMatch.getValues();

      //
      if (parameter.isRequired() && values.isEmpty()) {
        throw new CmdSyntaxException("Non satisfied " + parameter);
      }

      //
      Object v;
      if (parameter.getMultiplicity() == Multiplicity.LIST) {
        v = values;
      } else {
        if (values.isEmpty()) {
          continue;
        } else {
          v = values.get(0);
        }
      }

      //
      f.setAccessible(true);
      try {
        f.set(command, v);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    //
    return null;
  }
}
