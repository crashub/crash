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

package org.crsh.command.info;

import org.crsh.command.Argument;
import org.crsh.command.Description;
import org.crsh.command.Option;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandInfo<T> {

  public static <T> CommandInfo<T> create(Class<T> type) throws IntrospectionException {
    return new ClassCommandInfo<T>(type);
  }

  /** . */
  private final String name;

  /** . */
  private final String description;

  /** . */
  private final Map<String, OptionInfo> options;

  /** . */
  private final List<ArgumentInfo> arguments;

  CommandInfo(String name, String description, List<ParameterInfo> parameters) throws IntrospectionException {

    Map<String, OptionInfo> parameterMap = Collections.emptyMap();
    TreeMap<Integer, ArgumentInfo> argumentMap = new TreeMap<Integer, ArgumentInfo>();
    for (ParameterInfo parameter : parameters) {
      if (parameter instanceof OptionInfo) {
        OptionInfo option = (OptionInfo)parameter;
        for (String optionName : option.getNames()) {
          if (parameterMap.isEmpty()) {
            parameterMap = new HashMap<String, OptionInfo>();
          }
          parameterMap.put(optionName, option);
        }
      } else if (parameter instanceof ArgumentInfo) {
        ArgumentInfo argument = (ArgumentInfo)parameter;
        if (argumentMap.put(argument.getIndex(), argument) != null) {
          throw new IntrospectionException();
        }
      }
    }

    // Check consistency
    List<ArgumentInfo> arguments = Collections.emptyList();
    for (ArgumentInfo argument : argumentMap.values()) {
      if (arguments.isEmpty()) {
        arguments = new ArrayList<ArgumentInfo>();
      }
      arguments.add(argument);
    }

    //
    this.description = description;
    this.options = parameterMap;
    this.arguments = arguments;
    this.name = name;
  }

  public abstract Class<T> getType();

  public Iterable<OptionInfo> getOptions() {
    return options.values();
  }

  public OptionInfo getOption(String name) {
    return options.get(name);
  }

  public ArgumentInfo getArgument(int index) {
    for (ArgumentInfo argument : arguments) {
      if (argument.getIndex() >= index) {
        return argument;
      }
    }
    return null;
  }

  public List<ArgumentInfo> getArguments() {
    return arguments;
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

  protected static ParameterInfo create(
    Type type,
    Description descriptionAnn,
    Argument argumentAnn,
    Option optionAnn) throws IntrospectionException {
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }
      return new ArgumentInfo(
        type,
        argumentAnn.index(),
        description(descriptionAnn),
        argumentAnn.required(),
        argumentAnn.password());
    } else if (optionAnn != null) {
      return new OptionInfo(
        type,
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
