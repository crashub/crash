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

import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.ParameterDescriptor;
import org.crsh.cmdline.binding.TypeBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandMatch<C, D extends CommandDescriptor<C, B>, B extends TypeBinding> {

  /** . */
  private final List<OptionMatch<B>> optionMatches;

  /** . */
  private final List<ArgumentMatch<B>> argumentMatches;

  /** . */
  private final String rest;

  public CommandMatch(List<OptionMatch<B>> optionMatches, List<ArgumentMatch<B>> argumentMatches, String rest) {
    this.optionMatches = optionMatches;
    this.argumentMatches = argumentMatches;
    this.rest = rest;
  }

  public abstract D getDescriptor();

  public final Object invoke(InvocationContext context, C command) throws CmdLineException {

    //
    Map<ParameterDescriptor<?>, List<String>> abc = new HashMap<ParameterDescriptor<?>, List<String>>();

    //
    Set<ParameterDescriptor<?>> unused = getParameters();

    //
    for (ParameterMatch<?, ?> parameterMatch : getParameterMatches()) {
      ParameterDescriptor<?> parameter = parameterMatch.getParameter();
      if (!unused.remove(parameter)) {
        throw new CmdSyntaxException("Could not find parameter " + parameter);
      }
      abc.put(parameter, parameterMatch.getStrings());
    }

    // Convert values
    Map<ParameterDescriptor<?>, Object> parameterValues = new HashMap<ParameterDescriptor<?>, Object>();
    for (Map.Entry<ParameterDescriptor<?>, List<String>> entry : abc.entrySet()) {

      //
      ParameterDescriptor<?> parameter = entry.getKey();
      List<String> values = entry.getValue();

      // First convert the entire list
      List<Object> l = new ArrayList<Object>();
      for (String value : values) {
        try {
          Object o = parameter.parse(value);
          l.add(o);
        }
        catch (Exception e) {
          throw new CmdSyntaxException("Could not convert value " + value, e);
        }
      }

      //
      if (parameter.isRequired() && l.isEmpty()) {
        throw new CmdSyntaxException("Non satisfied " + parameter);
      }

      // Then figure out if we need to unwrap somehow
      Object v;
      if (parameter.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
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

    //
    return doInvoke(context, command, parameterValues);
  }

  protected abstract Object doInvoke(InvocationContext context, C command, Map<ParameterDescriptor<?>, Object> values) throws CmdLineException;

  public abstract Set<ParameterDescriptor<?>> getParameters();

  public abstract List<ParameterMatch<?, ?>> getParameterMatches();

  public abstract void printMan(Appendable writer) throws IOException;

  public abstract void printUsage(Appendable writer) throws IOException;

  public List<OptionMatch<B>> getOptionMatches() {
    return optionMatches;
  }

  public List<ArgumentMatch<B>> getArgumentMatches() {
    return argumentMatches;
  }

  public String getRest() {
    return rest;
  }
}
