/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.crsh.cli.descriptor;

import org.crsh.cli.impl.completion.CompletionMatcher;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.Multiplicity;
import org.crsh.cli.impl.invocation.CommandInvoker;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public abstract class CommandDescriptor<T> {

  /** . */
  private final String name;

  /** . */
  private final Description description;

  /** . */
  private final Map<String, OptionDescriptor> optionMap;

  /** . */
  private final Set<String> shortOptionNames;

  /** . */
  private final Set<String> longOptionNames;

  /** . */
  private boolean listArgument;

  /** . */
  private final List<OptionDescriptor> options;

  /** . */
  private final List<ArgumentDescriptor> arguments;

  /** . */
  private final List<ParameterDescriptor> parameters;

  /** . */
  private final Map<String, OptionDescriptor> uOptionMap;

  /** . */
  private final Set<String> uShortOptionNames;

  /** . */
  private final Set<String> uLongOptionNames;

  /** . */
  private final List<OptionDescriptor> uOptions;

  /** . */
  private final List<ArgumentDescriptor> uArguments;

  /** . */
  private final List<ParameterDescriptor> uParameters;

  protected CommandDescriptor(String name, Description description) throws IntrospectionException {

    //
    int nameLength = name.length();
    if (nameLength == 0) {
      throw new IntrospectionException("Command name cannot be null");
    } else {
      for (int i = 0;i < nameLength;i++) {
        char c = name.charAt(i);
        if (i == 0) {
          if (!Character.isLetter(c)) {
            throw new IntrospectionException("Invalid command name <" + name + "> does not start with a letter");
          }
        } else {
          if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_' && c != '-') {
            throw new IntrospectionException("Invalid command name <" + name + "> char " + c + " at position " + i + " is now allowed");
          }
        }
      }
    }

    //
    this.description = description;
    this.optionMap = new LinkedHashMap<String, OptionDescriptor>();
    this.arguments = new ArrayList<ArgumentDescriptor>();
    this.options = new ArrayList<OptionDescriptor>();
    this.name = name;
    this.parameters = new ArrayList<ParameterDescriptor>();
    this.listArgument = false;
    this.shortOptionNames = new HashSet<String>();
    this.longOptionNames = new HashSet<String>();

    //
    this.uOptionMap = Collections.unmodifiableMap(optionMap);
    this.uParameters = Collections.unmodifiableList(parameters);
    this.uOptions = Collections.unmodifiableList(options);
    this.uArguments = Collections.unmodifiableList(arguments);
    this.uShortOptionNames = shortOptionNames;
    this.uLongOptionNames = longOptionNames;
  }

  /**
   * Add a parameter to the command.
   *
   * @param parameter the parameter to add
   * @throws IntrospectionException any introspection exception that would prevent the parameter to be added
   * @throws NullPointerException if the parameter is null
   * @throws IllegalArgumentException if the parameter is already associated with another command
   */
  protected void addParameter(ParameterDescriptor parameter) throws IntrospectionException, NullPointerException, IllegalArgumentException {

    //
    if (parameter == null) {
      throw new NullPointerException("No null parameter accepted");
    }

    //
    if (parameter instanceof OptionDescriptor) {
      OptionDescriptor option = (OptionDescriptor)parameter;
      for (String optionName : option.getNames()) {
        String name;
        if (optionName.length() == 1) {
          name = "-" + optionName;
          if (shortOptionNames.contains(name)) {
            throw new IntrospectionException("Duplicate option " + name);
          } else {
            shortOptionNames.add(name);
          }
        } else {
          name = "--" + optionName;
          if (longOptionNames.contains(name)) {
            throw new IntrospectionException();
          } else {
            longOptionNames.add(name);
          }
        }
        optionMap.put(name, option);
      }
      options.add(option);
      ListIterator<ParameterDescriptor> i = parameters.listIterator();
      while (i.hasNext()) {
        ParameterDescriptor next = i.next();
        if (next instanceof ArgumentDescriptor) {
          i.previous();
          break;
        }
      }
      i.add(parameter);
    } else if (parameter instanceof ArgumentDescriptor) {
      ArgumentDescriptor argument = (ArgumentDescriptor)parameter;
      if (argument.getMultiplicity() == Multiplicity.MULTI) {
        if (listArgument) {
          throw new IntrospectionException();
        }
        listArgument = true;
      }
      arguments.add(argument);
      parameters.add(argument);
    } else {
      throw new AssertionError("Unreachable");
    }
  }

  public abstract CommandDescriptor<T> getOwner();

  public final int getDepth() {
    CommandDescriptor<T> owner = getOwner();
    return owner == null ? 0 : 1 + owner.getDepth();
  }


  public final void printUsage(Appendable to) throws IOException {
    print(Format.USAGE, to);
  }

  public final void printMan(Appendable to) throws IOException {
    print(Format.MAN, to);
  }

  public final void print(Format format, Appendable to) throws IOException {
    format.print(this, to);
  }

  /**
   * @return the command subordinates as a map.
   */
  public abstract Map<String, ? extends CommandDescriptor<T>> getSubordinates();

  /**
   * Returns a specified subordinate.
   *
   * @param name the subordinate name
   * @return the subordinate command or null
   */
  public final CommandDescriptor<T> getSubordinate(String name) {
    return getSubordinates().get(name);
  }

  /**
   * Returns the command parameters, the returned collection contains the command options and
   * the command arguments.
   *
   * @return the command parameters
   */
  public final List<ParameterDescriptor> getParameters() {
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
   * Returns the command short option names.
   *
   * @return the command long option names
   */
  public final Set<String> getShortOptionNames() {
    return uShortOptionNames;
  }

  /**
   * Returns the command long option names.
   *
   * @return the command long option names
   */
  public final Set<String> getLongOptionNames() {
    return uLongOptionNames;
  }

  /**
   * Returns the command options.
   *
   * @return the command options
   */
  public final Collection<OptionDescriptor> getOptions() {
    return uOptions;
  }

  /**
   * Returns a command option by its name.
   *
   * @param name the option name
   * @return the option
   */
  public final OptionDescriptor getOption(String name) {
    return optionMap.get(name);
  }

  /**
   * Find an command option by its name, this will look through the command hierarchy.
   *
   * @param name the option name
   * @return the option or null
   */
  public final OptionDescriptor resolveOption(String name) {
    OptionDescriptor option = getOption(name);
    if (option == null) {
      CommandDescriptor<T> owner = getOwner();
      if (owner != null) {
        option = owner.resolveOption(name);
      }
    }
    return option;
  }

  /**
   * Returns a list of the command arguments.
   *
   * @return the command arguments
   */
  public final List<ArgumentDescriptor> getArguments() {
    return uArguments;
  }

  /**
   * Returns a a specified argument by its index.
   *
   * @param index the argument index
   * @return the command argument
   * @throws IllegalArgumentException if the index is not within the bounds
   */
  public final ArgumentDescriptor getArgument(int index) throws IllegalArgumentException {
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

  public abstract CommandInvoker<T, ?> getInvoker(InvocationMatch<T> match);

  public final InvocationMatcher<T> matcher() {
    return new InvocationMatcher<T>(this);
  }

  public final CompletionMatcher<T> completer() {
    return new CompletionMatcher<T>(this);
  }

}
