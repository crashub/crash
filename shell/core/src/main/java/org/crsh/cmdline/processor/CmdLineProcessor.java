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

package org.crsh.cmdline.processor;

import org.crsh.cmdline.ClassCommandDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterBinding;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.analyzer.Analyzer;
import org.crsh.cmdline.analyzer.ArgumentMatch;
import org.crsh.cmdline.analyzer.CommandMatch;
import org.crsh.cmdline.analyzer.OptionMatch;
import org.crsh.cmdline.analyzer.ParameterMatch;
import static org.crsh.util.Utils.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CmdLineProcessor<T, C extends CommandDescriptor<T, B>, B extends ParameterBinding> {

  /** . */
  protected final CommandDescriptor<T, B> descriptor;

  /** . */
  protected final Analyzer<T, C, B> analyzer;

  public CmdLineProcessor(C commandDescriptor) {
    this.descriptor = commandDescriptor;
    this.analyzer = Analyzer.create(commandDescriptor);
  }

  public static class Clazz<T> extends CmdLineProcessor<T, ClassCommandDescriptor<T>, ParameterBinding.ClassField> {

    public Clazz(ClassCommandDescriptor<T> commandDescriptor) {
      super(commandDescriptor);
    }

    public void process(T object, String s) throws CmdLineException {

      //
      Set<ParameterDescriptor<?>> foo = newHashSet();
      foo.addAll(descriptor.getArguments());
      foo.addAll(descriptor.getOptions());

      //
      List<ParameterMatch<? extends ParameterDescriptor<ParameterBinding.ClassField>, ParameterBinding.ClassField>> invocation = newArrayList();
      CommandMatch<T, ClassCommandDescriptor<T>, ParameterBinding.ClassField> iterator = analyzer.analyze(s);

      //
      for (OptionMatch<ParameterBinding.ClassField> optionMatch : iterator.getOptionMatches()) {
        if (!foo.remove(optionMatch.getParameter())) {
          throw new SyntaxException();
        }
        invocation.add(optionMatch);
      }

      //
      for (ArgumentMatch<ParameterBinding.ClassField> argumentMatch : iterator.getArgumentMatches()) {
        if (!foo.remove(argumentMatch.getParameter())) {
          throw new SyntaxException();
        }
        invocation.add(argumentMatch);
      }

      // Should be better with required / non required
      for (ParameterDescriptor<?> nonSatisfied : foo) {
        if (!nonSatisfied.isRequired()) {
          // Ok
        } else {
          throw new SyntaxException("Non satisfied " + nonSatisfied);
        }
      }

      //
      for (ParameterMatch<? extends ParameterDescriptor<ParameterBinding.ClassField>, ParameterBinding.ClassField> parameterMatch : invocation) {

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
          f.set(object, v);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
