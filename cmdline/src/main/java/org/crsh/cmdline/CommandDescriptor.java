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

import org.crsh.cmdline.binding.TypeBinding;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes a command.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class CommandDescriptor<T, B extends TypeBinding> {

  /** . */
  private final String name;

  /** . */
  private final Description description;

  /** . */
  private final Map<String, OptionDescriptor<B>> optionMap;

  /** . */
  private final Set<OptionDescriptor<B>> options;

  /** . */
  private final List<ArgumentDescriptor<B>> arguments;

  /** . */
  private final List<ParameterDescriptor<B>> parameters;

  CommandDescriptor(
    String name,
    Description description,
    List<ParameterDescriptor<B>> parameters) throws IntrospectionException {

    Map<String, OptionDescriptor<B>> options = Collections.emptyMap();
    List<ArgumentDescriptor<B>> arguments = Collections.emptyList();
    boolean listArgument = false;
    for (ParameterDescriptor<B> parameter : parameters) {
      if (parameter instanceof OptionDescriptor) {
        OptionDescriptor<B> option = (OptionDescriptor<B>)parameter;
        for (String optionName : option.getNames()) {
          if (options.isEmpty()) {
            options = new LinkedHashMap<String, OptionDescriptor<B>>();
          }
          options.put((optionName.length() == 1 ? "-" : "--") + optionName, option);
        }
      } else if (parameter instanceof ArgumentDescriptor) {
        ArgumentDescriptor<B> argument = (ArgumentDescriptor<B>)parameter;
        if (argument.getMultiplicity() == Multiplicity.LIST) {
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
    this.options = options.isEmpty() ? Collections.<OptionDescriptor<B>>emptySet() : Collections.unmodifiableSet(new LinkedHashSet<OptionDescriptor<B>>(options.values()));
    this.name = name;
    this.parameters = parameters.isEmpty() ? Collections.<ParameterDescriptor<B>>emptyList() : Collections.<ParameterDescriptor<B>>unmodifiableList(new ArrayList<ParameterDescriptor<B>>(parameters));
  }

  public abstract Class<T> getType();

  public abstract void printUsage(PrintWriter writer);

  public abstract void printMan(PrintWriter writer);

  /**
   * Returns the command subordinates as a map.
   *
   * @return the subordinates
   */
  public abstract Map<String, ? extends CommandDescriptor<T, ?>> getSubordinates();

  /**
   * Returns the command parameters, the returned collection contains the command options and
   * the command arguments.
   *
   * @return the command parameters
   */
  public final Collection<ParameterDescriptor<B>> getParameters() {
    return parameters;
  }

  /**
   * Returns the command option names.
   *
   * @return the command option names
   */
  public final Set<String> getOptionNames() {
    return optionMap.keySet();
  }

  /**
   * Returns the command options.
   *
   * @return the command options
   */
  public final Collection<OptionDescriptor<B>> getOptions() {
    return options;
  }

  /**
   * Returns a command option by its name.
   *
   * @param name the option name
   * @return the option
   */
  public final OptionDescriptor<B> getOption(String name) {
    return optionMap.get(name);
  }

  /**
   * Returns a list of the command arguments.
   *
   * @return the command arguments
   */
  public final List<ArgumentDescriptor<B>> getArguments() {
    return arguments;
  }

  /**
   * Returns the command name.
   *
   * @return the command name
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the command description.
   *
   * @return the command description
   */
  public final Description getDescription() {
    return description;
  }

  /**
   * Returns the command usage, shortcut for invoking <code>getDescription().getUsage()</code> on this
   * object.
   *
   * @return the command usage
   */
  public final String getUsage() {
    return description != null ? description.getUsage() : "";
  }
}
