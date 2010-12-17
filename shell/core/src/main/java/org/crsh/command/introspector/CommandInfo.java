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

package org.crsh.command.introspector;

import org.crsh.command.Argument;
import org.crsh.command.Description;
import org.crsh.command.Option;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandInfo<T> {

  /** . */
  private final String name;

  /** . */
  private final String description;

  /** . */
  private final Map<String, ParameterInfo> options;

  public CommandInfo(String name, String description, List<ParameterInfo> parameters) {
    HashMap<String, ParameterInfo> parameterMap = new HashMap<String, ParameterInfo>();
    for (ParameterInfo parameter : parameters) {
      if (parameter instanceof OptionInfo) {
        OptionInfo option = (OptionInfo)parameter;
        for (String parameterName : option.getNames()) {
          parameterMap.put(parameterName, parameter);
        }
      }
    }

    //
    this.description = description;
    this.options = parameterMap;
    this.name = name;
  }

  public abstract Class<T> getType();

  public Iterable<ParameterInfo> getOptions() {
    return options.values();
  }

  public ParameterInfo getOption(String name) {
    return options.get(name);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  protected static String description(Description descriptionAnn) {
    return descriptionAnn != null ? descriptionAnn.value() : "";
  }

  protected static ParameterInfo create(Description descriptionAnn, Argument argumentAnn, Option optionAnn) throws IntrospectionException {
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }
      return new ArgumentInfo(
        argumentAnn.index(),
        description(descriptionAnn),
        argumentAnn.required(),
        argumentAnn.arity(),
        argumentAnn.password());
    } else if (optionAnn != null) {
      return new OptionInfo(
        Collections.unmodifiableList(Arrays.asList(optionAnn.names())),
        description(descriptionAnn),
        optionAnn.required(),
        optionAnn.arity(),
        optionAnn.password());
    } else {
      return null;
    }
  }
}
