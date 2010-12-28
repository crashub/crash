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

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.binding.ClassFieldBinding;
import org.crsh.cmdline.binding.MethodArgumentBinding;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MethodMatch<T> extends CommandMatch<T, MethodDescriptor<T>, MethodArgumentBinding> {

  /** . */
  private final MethodDescriptor<T> descriptor;

  /** . */
  private final ClassMatch<T> owner;

  public MethodMatch(
    ClassMatch<T> owner,
    MethodDescriptor<T> descriptor,
    List<OptionMatch<MethodArgumentBinding>> optionMatches,
    List<ArgumentMatch<MethodArgumentBinding>> argumentMatches,
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
  public Object invoke(InvocationContext context, T command) throws CmdLineException {

    //
    List<ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding>> classParameters = new ArrayList<ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding>>();
    Map<Integer, ParameterMatch<? extends ParameterDescriptor<MethodArgumentBinding>, MethodArgumentBinding>> methodParameters = new HashMap<Integer, ParameterMatch<? extends ParameterDescriptor<MethodArgumentBinding>, MethodArgumentBinding>>();

    //
    Map<ParameterDescriptor<?>, List<String>> abc = new HashMap<ParameterDescriptor<?>, List<String>>();

    //
    Set<ParameterDescriptor<?>> unused = new HashSet<ParameterDescriptor<?>>();
    unused.addAll(descriptor.getArguments());
    unused.addAll(descriptor.getOptions());
    unused.addAll(owner.getDescriptor().getOptions());

    //
    for (OptionMatch<MethodArgumentBinding> optionMatch : getOptionMatches()) {
      OptionDescriptor<MethodArgumentBinding> parameter = optionMatch.getParameter();
      if (!unused.remove(parameter)) {
        throw new CmdSyntaxException();
      }
      methodParameters.put(parameter.getBinding().getIndex(), optionMatch);
      abc.put(parameter, optionMatch.getValues());
    }

    //
    for (ArgumentMatch<MethodArgumentBinding> argumentMatch : getArgumentMatches()) {
      ArgumentDescriptor<MethodArgumentBinding> parameter = argumentMatch.getParameter();
      if (!unused.remove(parameter)) {
        throw new CmdSyntaxException();
      }
      methodParameters.put(parameter.getBinding().getIndex(), argumentMatch);
      abc.put(parameter, argumentMatch.getValues());
    }

    //
    for (OptionMatch<ClassFieldBinding> optionMatch : owner.getOptionMatches()) {
      OptionDescriptor<ClassFieldBinding> parameter = optionMatch.getParameter();
      if (!unused.remove(parameter)) {
        throw new CmdSyntaxException();
      }
      classParameters.add(optionMatch);
      abc.put(parameter, optionMatch.getValues());
    }

    // Should be better with required / non required
    for (ParameterDescriptor<?> nonSatisfied : unused) {
      if (!nonSatisfied.isRequired()) {
        // Ok
      } else {
        throw new CmdSyntaxException("Non satisfied " + nonSatisfied);
      }
    }

    // Convert values
    Map<ParameterDescriptor<?>, Object> parameterValues = new HashMap<ParameterDescriptor<?>, Object>();
    for (Map.Entry<ParameterDescriptor<?>, List<String>> entry : abc.entrySet()) {

      //
      ParameterDescriptor<?> parameter = entry.getKey();
      List<String> value = entry.getValue();

      // First convert the entire list
      List<Object> l = new ArrayList<Object>();
      for (String s : value) {
        Object o = parameter.parse(s);
        l.add(o);
      }

      //
      if (parameter.isRequired() && l.isEmpty()) {
        throw new CmdSyntaxException("Non satisfied " + parameter);
      }

      // Then figure out if we need to unwrap somehow
      Object v;
      if (parameter.getMultiplicity() == Multiplicity.LIST) {
        v = l;
      } else {
        if (l.isEmpty()) {
          continue;
        } else {
          v = l.get(0);
        }
      }

      //
      parameterValues.put(parameter, v);
    }

    // Prepare invocation
    MethodDescriptor<T> descriptor = getDescriptor();
    Method m = descriptor.getMethod();
    Class<?>[] parameterTypes = m.getParameterTypes();
    Object[] mArgs = new Object[parameterTypes.length];
    for (int i = 0;i < mArgs.length;i++) {
      ParameterMatch<? extends ParameterDescriptor<MethodArgumentBinding>, MethodArgumentBinding> parameterMatch = methodParameters.get(i);

      //
      Object v;
      if (parameterMatch == null) {
        Class<?> parameterType = parameterTypes[i];
        if (parameterType.isPrimitive()) {
          throw new UnsupportedOperationException("Todo : primitive handling");
        } else {
          // Attempt to obtain from invocation context
          v = context.getAttribute(parameterType);
        }
      } else {
        v = parameterValues.get(parameterMatch.getParameter());
      }

      //
      mArgs[i] = v;
    }

    //
    try {
      // First configure command
      for (ParameterMatch<? extends ParameterDescriptor<ClassFieldBinding>, ClassFieldBinding> parameterMatch : classParameters) {
        ParameterDescriptor<ClassFieldBinding> parameter = parameterMatch.getParameter();
        Object value = parameterValues.get(parameter);
        Field f = parameter.getBinding().getField();
        f.setAccessible(true);
        f.set(command, value);
      }

      //
      return m.invoke(command, mArgs);
    }
    catch (Exception e) {
      throw new CmdInvocationException(e.getMessage(), e);
    }
  }
}
