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

import org.crsh.cli.impl.lang.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

/**
 * Format the command descriptor for producing documentation.
 *
 * @author Julien Viet
 */
public abstract class Format {

  /** . */
  public static final Describe DESCRIBE = new Describe();

  /** . */
  public static final Usage USAGE = new Usage();

  /** . */
  public static final Man MAN = new Man();

  /**
   * Print the specified <code>command</code> to the <code>stream</code>
   * @param command the command to print
   * @param stream the output
   * @throws IOException
   */
  public abstract void print(CommandDescriptor<?> command, Appendable stream) throws IOException;

  /**
   * Print the full qualified name of the command.
   *
   * @param command the command
   * @param stream the output
   * @throws IOException any io exception
   */
  protected void printFQN(CommandDescriptor<?> command, Appendable stream) throws IOException {
    CommandDescriptor<?> owner = command.getOwner();
    if (owner != null) {
      printFQN(owner, stream);
      stream.append(' ');
    }
    stream.append(command.getName());
  }

  protected void printFQNWithOptions(CommandDescriptor<?> command, Appendable stream) throws IOException {
    CommandDescriptor<?> owner = command.getOwner();
    if (owner != null) {
      printFQNWithOptions(owner, stream);
      stream.append(' ');
    }
    stream.append(command.getName());
    for (OptionDescriptor option : command.getOptions()) {
      stream.append(' ');
      option.printUsage(stream);
    }
  }

  /**
   * The command description in one line.
   */
  public static class Describe extends Format {
    @Override
    public void print(CommandDescriptor<?> command, Appendable stream) throws IOException {
      stream.append(command.getUsage());
    }
  }

  /**
   * The command manual.
   */
  public static class Man extends Format {

    public void print(CommandDescriptor<?> command, Appendable stream) throws IOException {
      printNameSection(command, stream);
      printSynopsisSection(command, stream);
      printDescriptionSection(command, stream);
      printParametersSection(command, stream);
    }

    public void printNameSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      stream.append("NAME\n");
      stream.append(Util.MAN_TAB);
      printFQN(command, stream);
      String usage = command.getUsage();
      if (usage.length() > 0) {
        stream.append(" - ").append(usage);
      }
      stream.append("\n\n");
    }

    public void printSynopsisSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      stream.append("SYNOPSIS\n");
      stream.append(Util.MAN_TAB);
      printFQNWithOptions(command, stream);
      if (command.getSubordinates().size() > 0) {
        stream.append(" COMMAND [ARGS]");
      } else {
        for (ArgumentDescriptor argument : command.getArguments()) {
          stream.append(' ');
          argument.printUsage(stream);
        }
      }
      stream.append("\n\n");
    }

    public void printDescriptionSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      String man = command.getDescription().getMan();
      if (man.length() > 0) {
        stream.append("DESCRIPTION\n");
        Util.indent(Util.MAN_TAB, man, stream);
        stream.append("\n\n");
      }
    }

    public void printParametersSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      boolean printed = printOptions(false, command, stream);
      if (command.getSubordinates().size() > 0) {
        stream.append("COMMANDS\n");
        printSubordinates(command, stream);
      } else {
        printParameters(printed, command, stream);
      }
    }

    protected void printSubordinates(CommandDescriptor<?> command, Appendable stream) throws IOException {
      for (CommandDescriptor<?> subordinate : command.getSubordinates().values()) {
        stream.append(Util.MAN_TAB).append(subordinate.getName());
        String methodText = subordinate.getDescription().getBestEffortMan();
        if (methodText.length() > 0) {
          stream.append("\n");
          Util.indent(Util.MAN_TAB_EXTRA, methodText, stream);
        }
        stream.append("\n\n");
      }
    }

    protected boolean printOptions(boolean printed, CommandDescriptor<?> command, Appendable stream) throws IOException {
      CommandDescriptor<?> owner = command.getOwner();
      if (owner != null) {
        printed = printOptions(printed, owner, stream);
      }
      for (OptionDescriptor option : command.getOptions()) {
        printed = printParameter(printed, option, stream);
      }
      return printed;
    }

    protected boolean printParameters(boolean printed, CommandDescriptor<?> command, Appendable stream) throws IOException {
      for (ArgumentDescriptor argument : command.getArguments()) {
        printed = printParameter(printed, argument, stream);
      }
      return printed;
    }

    protected boolean printParameter(boolean printed, ParameterDescriptor parameter, Appendable stream) throws IOException {
      if (!printed) {
        stream.append("PARAMETERS\n");
      }
      stream.append(Util.MAN_TAB);
      parameter.printUsage(stream);
      String parameterText = parameter.getDescription().getBestEffortMan();
      if (parameterText.length() > 0) {
        stream.append("\n");
        Util.indent(Util.MAN_TAB_EXTRA, parameterText, stream);
      }
      stream.append("\n\n");
      return true;
    }
  }

  /**
   * The command usage.
   */
  public static class Usage extends Format {

    public void print(CommandDescriptor<?> command, Appendable stream) throws IOException {
      printUsageSection(command, stream);
      printDetailsSection(command, stream);
    }

    public void printUsageSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      stream.append("usage: ");
      printFQNWithOptions(command, stream);
      if (command.getSubordinates().size() > 0) {
        stream.append(" COMMAND [ARGS]");
      } else {
        for (ArgumentDescriptor argument : command.getArguments()) {
          stream.append(' ');
          argument.printUsage(stream);
        }
      }
      stream.append("\n\n");
    }

    private List<String[]> collectParametersTuples(CommandDescriptor<?> command) throws IOException {
      CommandDescriptor<?> owner = command.getOwner();
      List<String[]> tuples;
      Collection<? extends ParameterDescriptor> parameters;
      if (owner != null) {
        tuples = collectParametersTuples(owner);
        parameters = command.getOptions();
      } else {
        tuples = new ArrayList<String[]>();
        parameters = command.getParameters();
      }
      for (ParameterDescriptor parameter : parameters) {
        StringBuilder sb = new StringBuilder();
        parameter.printUsage(sb);
        String usage = sb.toString();
        tuples.add(new String[]{usage, parameter.getUsage()});
      }
      return tuples;
    }

    public void printDetailsSection(CommandDescriptor<?> command, Appendable stream) throws IOException {
      if (command.getSubordinates().isEmpty()) {
        List<String[]> tt = collectParametersTuples(command);
        int length = 0;
        for (String[] s : tt) {
          length = Math.max(s[0].length(), length);
        }
        String format = "   %1$-" + length + "s %2$s\n";
        for (String[] tuple : tt) {
          Formatter formatter = new Formatter(stream);
          formatter.format(format, tuple[0], tuple[1]);
        }
      } else {
        stream.append("The most commonly used ").append(command.getName()).append(" commands are:\n");
        String format = "   %1$-16s %2$s\n";
        for (CommandDescriptor<?> subordinate : command.getSubordinates().values()) {
          Formatter formatter = new Formatter(stream);
          formatter.format(format, subordinate.getName(), subordinate.getUsage());
        }
      }
      stream.append("\n\n");
    }
  }
}
