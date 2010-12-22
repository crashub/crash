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

import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterBinding;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.processor.CmdInvocationException;
import org.crsh.cmdline.processor.CmdLineException;
import org.crsh.cmdline.processor.CmdSyntaxException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.crsh.util.Utils.newHashMap;
import static org.crsh.util.Utils.newHashSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MethodMatch<T> extends CommandMatch<T, MethodDescriptor<T>, ParameterBinding.MethodArgument> {

  /** . */
  private final MethodDescriptor<T> descriptor;

  /** . */
  private final ClassMatch<T> owner;

  public MethodMatch(
    ClassMatch<T> owner,
    MethodDescriptor<T> descriptor,
    List<OptionMatch<ParameterBinding.MethodArgument>> optionMatches,
    List<ArgumentMatch<ParameterBinding.MethodArgument>> argumentMatches,
    String rest) {
    super(optionMatches, argumentMatches, rest);

    //
    this.owner = owner;
    this.descriptor = descriptor;
  }

  @Override
  public MethodDescriptor<T> getDescriptor() {
    return descriptor;
  }

  public ClassMatch<T> getOwner() {
    return owner;
  }

  @Override
  public void process(T command) throws CmdLineException {

    // Configure command

    //
    Map<Integer, ParameterMatch<? extends ParameterDescriptor<ParameterBinding.MethodArgument>, ParameterBinding.MethodArgument>> used = newHashMap();
    Set<ParameterDescriptor<?>> unused = newHashSet();
    unused.addAll(descriptor.getArguments());
    unused.addAll(descriptor.getOptions());

    //
    for (OptionMatch<ParameterBinding.MethodArgument> optionMatch : getOptionMatches()) {
      if (!unused.remove(optionMatch.getParameter())) {
        throw new CmdSyntaxException();
      }
      used.put(optionMatch.getParameter().getBinding().getIndex(), optionMatch);
    }

    //
    for (ArgumentMatch<ParameterBinding.MethodArgument> argumentMatch : getArgumentMatches()) {
      if (!unused.remove(argumentMatch.getParameter())) {
        throw new CmdSyntaxException();
      }
      used.put(argumentMatch.getParameter().getBinding().getIndex(), argumentMatch);
    }

    // Should be better with required / non required
    for (ParameterDescriptor<?> nonSatisfied : unused) {
      if (!nonSatisfied.isRequired()) {
        // Ok
      } else {
        throw new CmdSyntaxException("Non satisfied " + nonSatisfied);
      }
    }

    // Prepare invocation
    MethodDescriptor<T> descriptor = getDescriptor();
    Method m = descriptor.getMethod();
    Class<?>[] parameterTypes = m.getParameterTypes();
    Object[] mArgs = new Object[parameterTypes.length];
    for (int i = 0;i < mArgs.length;i++) {
      ParameterMatch<? extends ParameterDescriptor<ParameterBinding.MethodArgument>, ParameterBinding.MethodArgument> parameterMatch = used.get(i);

      //
      Object v;
      if (parameterMatch == null) {
        if (parameterTypes[i].isPrimitive()) {
          throw new UnsupportedOperationException("Todo : primitive handling");
        } else {
          v = null;
        }
      }
      else {
        ParameterDescriptor<ParameterBinding.MethodArgument> parameter = parameterMatch.getParameter();
        if (parameter.getType().getMultiplicity() == Multiplicity.LIST) {
          v = parameterMatch.getValues();
        } else {
          v = parameterMatch.getValues().get(0);
        }
      }

      //
      mArgs[i] = v;
    }

    // First configure command
    owner.process(command);

    //
    try {
      m.invoke(command, mArgs);
    }
    catch (Exception e) {
      throw new CmdInvocationException(e.getMessage(), e);
    }
  }
}
