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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
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
  private boolean listArgument;

  /** . */
  private final List<ArgumentDescriptor<B>> arguments;

  /** . */
  private final List<ParameterDescriptor<B>> parameters;

  /** . */
  private final Map<String, OptionDescriptor<B>> uOptionMap;

  /** . */
  private final List<ArgumentDescriptor<B>> uArguments;

  /** . */
  private final List<ParameterDescriptor<B>> uParameters;

  CommandDescriptor(String name, Description description) throws IntrospectionException {

    //
    this.description = description;
    this.optionMap = new LinkedHashMap<String, OptionDescriptor<B>>();
    this.arguments = new ArrayList<ArgumentDescriptor<B>>();
    this.name = name;
    this.parameters = new ArrayList<ParameterDescriptor<B>>();
    this.listArgument = false;

    //
    this.uOptionMap = Collections.unmodifiableMap(optionMap);
    this.uParameters = Collections.unmodifiableList(parameters);
    this.uArguments = Collections.unmodifiableList(arguments);
  }

  void addParameter(ParameterDescriptor<B> parameter) throws IntrospectionException {

    //
    if (parameter.owner != null) {
      throw new IllegalStateException("The parameter is already associated with a command");
    }

    //
    if (parameter instanceof OptionDescriptor) {
      OptionDescriptor<B> option = (OptionDescriptor<B>)parameter;
      for (String optionName : option.getNames()) {
        optionMap.put((optionName.length() == 1 ? "-" : "--") + optionName, option);
      }
      ListIterator<ParameterDescriptor<B>> i = parameters.listIterator();
      while (i.hasNext()) {
        ParameterDescriptor<B> next = i.next();
        if (next instanceof ArgumentDescriptor<?>) {
          i.previous();
          break;
        }
      }
      i.add(parameter);
      parameter.owner = this;
    } else if (parameter instanceof ArgumentDescriptor) {
      ArgumentDescriptor<B> argument = (ArgumentDescriptor<B>)parameter;
      if (argument.getMultiplicity() == Multiplicity.ZERO_OR_MORE) {
        if (listArgument) {
          throw new IntrospectionException();
        }
        listArgument = true;
      }
      arguments.add(argument);
      parameters.add(argument);
      parameter.owner = this;
    }
  }

  public abstract Class<T> getType();

  public abstract void printUsage(Appendable writer) throws IOException;

  public abstract void printMan(Appendable writer) throws IOException;

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
    return uParameters;
  }

  /**
   * Returns the command option names.
   *
   * @return the command option names
   */
  public final Set<String> getOptionNames() {
    return uOptionMap.keySet();
  }

  /**
   * Returns the command options.
   *
   * @return the command options
   */
  public final Collection<OptionDescriptor<B>> getOptions() {
    return uOptionMap.values();
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
   * Find an command option by its name.
   *
   * @param name the option name
   * @return the option
   */
  public abstract OptionDescriptor<?> findOption(String name);

  /**
   * Returns a list of the command arguments.
   *
   * @return the command arguments
   */
  public final List<ArgumentDescriptor<B>> getArguments() {
    return uArguments;
  }

  /**
   * Returns a a specified argument by its index.
   *
   * @return the command argument
   * @throws IllegalArgumentException if the index is not within the bounds
   */
  public final ArgumentDescriptor<B> getArgument(int index) throws IllegalArgumentException {
    if (index < 0) {
      throw new IllegalArgumentException();
    }
    if (index >= arguments.size()) {
      throw new IllegalArgumentException();
    }
    return arguments.get(index);
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
