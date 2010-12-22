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

package org.crsh.cmdline;

import org.crsh.command.Description;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandDescriptor<T, B extends ParameterBinding> {

  public static <T> ClassDescriptor<T> create(Class<T> type) throws IntrospectionException {
    return new ClassDescriptor<T>(type);
  }

  /** . */
  private final String name;

  /** . */
  private final String description;

  /** . */
  private final Map<String, OptionDescriptor<B>> optionMap;

  /** . */
  private final Set<OptionDescriptor<B>> options;

  /** . */
  private final List<ArgumentDescriptor<B>> arguments;

  CommandDescriptor(String name, String description, List<ParameterDescriptor<B>> parameters) throws IntrospectionException {

    Map<String, OptionDescriptor<B>> options = Collections.emptyMap();
    List<ArgumentDescriptor<B>> arguments = Collections.emptyList();
    boolean listArgument = false;
    for (ParameterDescriptor<B> parameter : parameters) {
      if (parameter instanceof OptionDescriptor) {
        OptionDescriptor<B> option = (OptionDescriptor<B>)parameter;
        for (String optionName : option.getNames()) {
          if (options.isEmpty()) {
            options = new HashMap<String, OptionDescriptor<B>>();
          }
          options.put((optionName.length() == 1 ? "-" : "--") + optionName, option);
        }
      } else if (parameter instanceof ArgumentDescriptor) {
        ArgumentDescriptor<B> argument = (ArgumentDescriptor<B>)parameter;
        if (argument.getType().getMultiplicity() == Multiplicity.LIST) {
          if (listArgument) {
            throw new IntrospectionException();
          }
          listArgument = true;
        }
        if (arguments.isEmpty()) {
          arguments = new ArrayList<ArgumentDescriptor<B>>();
        }
        arguments.add(argument);
      }
    }

    //
    this.description = description;
    this.optionMap = options.isEmpty() ? options : Collections.unmodifiableMap(options);
    this.arguments = arguments.isEmpty() ? arguments : Collections.unmodifiableList(arguments);
    this.options = options.isEmpty() ? Collections.<OptionDescriptor<B>>emptySet() : Collections.unmodifiableSet(new HashSet<OptionDescriptor<B>>(options.values()));
    this.name = name;
  }

  public abstract Class<T> getType();

  public Set<String> getOptionNames() {
    return optionMap.keySet();
  }

  public Collection<OptionDescriptor<B>> getOptions() {
    return options;
  }

  public OptionDescriptor<B> getOption(String name) {
    return optionMap.get(name);
  }

  public List<ArgumentDescriptor<B>> getArguments() {
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

  protected static <B extends ParameterBinding> ParameterDescriptor<B> create(
    B binding,
    Type type,
    Description descriptionAnn,
    Argument argumentAnn,
    Option optionAnn) throws IntrospectionException {
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }
      return new ArgumentDescriptor<B>(
        binding,
        type,
        description(descriptionAnn),
        argumentAnn.required(),
        argumentAnn.password());
    } else if (optionAnn != null) {
      return new OptionDescriptor<B>(
        binding,
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
