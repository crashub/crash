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

package org.crsh.cmdline.analyzer;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterBinding;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.processor.CmdLineException;
import org.crsh.cmdline.processor.CmdSyntaxException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.crsh.util.Utils.newArrayList;
import static org.crsh.util.Utils.newHashSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClassMatch<T> extends CommandMatch<T, ClassDescriptor<T>, ParameterBinding.ClassField> {

  /** . */
  private final ClassDescriptor<T> descriptor;

  public ClassMatch(
    ClassDescriptor<T> descriptor,
    List<OptionMatch<ParameterBinding.ClassField>> optionMatches,
    List<ArgumentMatch<ParameterBinding.ClassField>> argumentMatches,
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
  public Object invoke(T command) throws CmdLineException {
    List<ParameterMatch<? extends ParameterDescriptor<ParameterBinding.ClassField>, ParameterBinding.ClassField>> used = newArrayList();
    Set<ParameterDescriptor<?>> unused = newHashSet();
    unused.addAll(descriptor.getArguments());
    unused.addAll(descriptor.getOptions());

    //
    for (OptionMatch<ParameterBinding.ClassField> optionMatch : getOptionMatches()) {
      if (!unused.remove(optionMatch.getParameter())) {
        throw new CmdSyntaxException();
      }
      used.add(optionMatch);
    }

    //
    for (ArgumentMatch<ParameterBinding.ClassField> argumentMatch : getArgumentMatches()) {
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
    for (ParameterMatch<? extends ParameterDescriptor<ParameterBinding.ClassField>, ParameterBinding.ClassField> parameterMatch : used) {

      ParameterDescriptor<ParameterBinding.ClassField> parameter = parameterMatch.getParameter();
      ParameterBinding.ClassField cf = parameter.getBinding();
      Field f = cf.getField();

      //
      Object v;
      if (parameter.getType().getMultiplicity() == Multiplicity.LIST) {
        v = parameterMatch.getValues();
      } else {
        v = parameterMatch.getValues().get(0);
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
