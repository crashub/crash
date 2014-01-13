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
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static org.crsh.cli.impl.lang.Util.tuples;

public abstract class CommandDescriptor<T> {

  /** . */
  private static final Set<String> MAIN_SINGLETON = Collections.singleton("main");

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
          shortOptionNames.add(name);
        } else {
          name = "--" + optionName;
          longOptionNames.add(name);
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
    }
  }

  public abstract Class<T> getType();

  public abstract CommandDescriptor<T> getOwner();

  public final int getDepth() {
    CommandDescriptor<T> owner = getOwner();
    return owner == null ? 0 : 1 + owner.getDepth();
  }

  public final void printUsage(Appendable writer) throws IOException {
    int depth = getDepth();
    switch (depth) {
      case 0: {
        Map<String, ? extends CommandDescriptor<T>> methods = getSubordinates();
        if (methods.size() == 1) {
          methods.values().iterator().next().printUsage(writer);
        } else {
          writer.append("usage: ").append(getName());
          for (OptionDescriptor option : getOptions()) {
            option.printUsage(writer);
          }
          writer.append(" COMMAND [ARGS]\n\n");
          writer.append("The most commonly used ").append(getName()).append(" commands are:\n");
          String format = "   %1$-16s %2$s\n";
          for (CommandDescriptor<T> method : methods.values()) {
            Formatter formatter = new Formatter(writer);
            formatter.format(format, method.getName(), method.getUsage());
          }
        }
        break;
      }
      case 1: {

        CommandDescriptor<T> owner = getOwner();
        int length = 0;
        List<String> parameterUsages = new ArrayList<String>();
        List<String> parameterBilto = new ArrayList<String>();
        boolean printName = !owner.getSubordinates().keySet().equals(MAIN_SINGLETON);

        //
        writer.append("usage: ").append(owner.getName());

        //
        for (OptionDescriptor option : owner.getOptions()) {
          writer.append(" ");
          StringBuilder sb = new StringBuilder();
          option.printUsage(sb);
          String usage = sb.toString();
          writer.append(usage);

          length = Math.max(length, usage.length());
          parameterUsages.add(usage);
          parameterBilto.add(option.getUsage());
        }

        //
        writer.append(printName ? (" " + getName()) : "");

        //
        for (ParameterDescriptor parameter : getParameters()) {
          writer.append(" ");
          StringBuilder sb = new StringBuilder();
          parameter.printUsage(sb);
          String usage = sb.toString();
          writer.append(usage);

          length = Math.max(length, usage.length());
          parameterBilto.add(parameter.getUsage());
          parameterUsages.add(usage);
        }
        writer.append("\n\n");

        //
        String format = "   %1$-" + length + "s %2$s\n";
        for (String[] tuple : tuples(String.class, parameterUsages, parameterBilto)) {
          Formatter formatter = new Formatter(writer);
          formatter.format(format, tuple[0], tuple[1]);
        }

        //
        writer.append("\n\n");
        break;
      }
      default:
        throw new UnsupportedOperationException("Does not make sense");
    }


  }

  public final void printMan(Appendable writer) throws IOException {
    int depth = getDepth();
    switch (depth) {
      case 0: {
        Map<String, ? extends CommandDescriptor<T>> methods = getSubordinates();
        if (methods.size() == 1) {
          methods.values().iterator().next().printMan(writer);
        } else {

          // Name
          writer.append("NAME\n");
          writer.append(Util.MAN_TAB).append(getName());
          if (getUsage().length() > 0) {
            writer.append(" - ").append(getUsage());
          }
          writer.append("\n\n");

          // Synopsis
          writer.append("SYNOPSIS\n");
          writer.append(Util.MAN_TAB).append(getName());
          for (OptionDescriptor option : getOptions()) {
            writer.append(" ");
            option.printUsage(writer);
          }
          writer.append(" COMMAND [ARGS]\n\n");

          //
          String man = getDescription().getMan();
          if (man.length() > 0) {
            writer.append("DESCRIPTION\n");
            Util.indent(Util.MAN_TAB, man, writer);
            writer.append("\n\n");
          }

          // Common options
          if (getOptions().size() > 0) {
            writer.append("PARAMETERS\n");
            for (OptionDescriptor option : getOptions()) {
              writer.append(Util.MAN_TAB);
              option.printUsage(writer);
              String optionText = option.getDescription().getBestEffortMan();
              if (optionText.length() > 0) {
                writer.append("\n");
                Util.indent(Util.MAN_TAB_EXTRA, optionText, writer);
              }
              writer.append("\n\n");
            }
          }

          //
          writer.append("COMMANDS\n");
          for (CommandDescriptor<T> method : methods.values()) {
            writer.append(Util.MAN_TAB).append(method.getName());
            String methodText = method.getDescription().getBestEffortMan();
            if (methodText.length() > 0) {
              writer.append("\n");
              Util.indent(Util.MAN_TAB_EXTRA, methodText, writer);
            }
            writer.append("\n\n");
          }
        }
        break;
      }
      case 1: {

        CommandDescriptor<T> owner = getOwner();

        //
        boolean printName = !owner.getSubordinates().keySet().equals(MAIN_SINGLETON);

        // Name
        writer.append("NAME\n");
        writer.append(Util.MAN_TAB).append(owner.getName());
        if (printName) {
          writer.append(" ").append(getName());
        }
        if (getUsage().length() > 0) {
          writer.append(" - ").append(getUsage());
        }
        writer.append("\n\n");

        // Synopsis
        writer.append("SYNOPSIS\n");
        writer.append(Util.MAN_TAB).append(owner.getName());
        for (OptionDescriptor option : owner.getOptions()) {
          writer.append(" ");
          option.printUsage(writer);
        }
        if (printName) {
          writer.append(" ").append(getName());
        }
        for (OptionDescriptor option : getOptions()) {
          writer.append(" ");
          option.printUsage(writer);
        }
        for (ArgumentDescriptor argument : getArguments()) {
          writer.append(" ");
          argument.printUsage(writer);
        }
        writer.append("\n\n");

        // Description
        String man = getDescription().getMan();
        if (man.length() > 0) {
          writer.append("DESCRIPTION\n");
          Util.indent(Util.MAN_TAB, man, writer);
          writer.append("\n\n");
        }

        // Parameters
        List<OptionDescriptor> options = new ArrayList<OptionDescriptor>();
        options.addAll(owner.getOptions());
        options.addAll(getOptions());
        if (options.size() > 0) {
          writer.append("\nPARAMETERS\n");
          for (ParameterDescriptor parameter : Util.join(owner.getOptions(), getParameters())) {
            writer.append(Util.MAN_TAB);
            parameter.printUsage(writer);
            String parameterText = parameter.getDescription().getBestEffortMan();
            if (parameterText.length() > 0) {
              writer.append("\n");
              Util.indent(Util.MAN_TAB_EXTRA, parameterText, writer);
            }
            writer.append("\n\n");
          }
        }

        //
        break;
      }
      default:
        throw new UnsupportedOperationException("Does not make sense");
    }
  }


  /**
   * Returns the command subordinates as a map.
   *
   * @return the subordinates
   */
  public abstract Map<String, ? extends CommandDescriptor<T>> getSubordinates();

  public abstract CommandDescriptor<T> getSubordinate(String name);

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
  public final OptionDescriptor findOption(String name) {
    OptionDescriptor option = getOption(name);
    if (option == null) {
      CommandDescriptor<T> owner = getOwner();
      if (owner != null) {
        option = owner.findOption(name);
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

  public final InvocationMatcher<T> matcher() {
    return matcher(null);
  }

  public abstract InvocationMatcher<T> matcher(String mainName);

  public final CompletionMatcher<T> completer() {
    return completer(null);
  }

  public abstract CompletionMatcher<T> completer(String mainName);

}
