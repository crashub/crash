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

package org.crsh.cmdline;

import org.crsh.cmdline.binding.MethodArgumentBinding;

import static org.crsh.cmdline.Util.indent;
import static org.crsh.cmdline.Util.tuples;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodDescriptor<T> extends CommandDescriptor<T, MethodArgumentBinding> {

  /** . */
  private static final Set<String> MAIN_SINGLETON = Collections.singleton("main");

  /** . */
  private final ClassDescriptor<T> owner;

  /** . */
  private final Method method;

  /** . */
  private final int size;

  MethodDescriptor(
    ClassDescriptor<T> owner,
    Method method,
    String name,
    Description info) throws IntrospectionException {
    super(name, info);

    //
    this.owner = owner;
    this.method = method;
    this.size = method.getParameterTypes().length;
  }

  /**
   * Returns the parameter descriptor for the specified method parameter index.
   *
   * @param index the parameter index
   * @return the parameter descriptor or null if none can be bound
   * @throws IndexOutOfBoundsException if the index is not valid
   */
  public ParameterDescriptor<MethodArgumentBinding> getParameter(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Bad index value " + index);
    }
    for (ParameterDescriptor<MethodArgumentBinding> argument : getParameters()) {
      if (argument.getBinding().getIndex() == index) {
        return argument;
      }
    }
    return null;
  }

  @Override
  public Map<String, ? extends CommandDescriptor<T, ?>> getSubordinates() {
    return Collections.emptyMap();
  }

  public Method getMethod() {
    return method;
  }

  @Override
  public Class<T> getType() {
    return owner.getType();
  }

  @Override
  public OptionDescriptor<?> findOption(String name) {
    OptionDescriptor<?> option = getOption(name);
    if (option == null) {
      option = owner.findOption(name);
    }
    return option;
  }

  @Override
  public void printUsage(Appendable writer) throws IOException {
    int length = 0;
    List<String> parameterUsages = new ArrayList<String>();
    List<String> parameterBilto = new ArrayList<String>();
    boolean printName = !owner.getSubordinates().keySet().equals(MAIN_SINGLETON);

    //
    writer.append("usage: ").append(owner.getName());

    //
    for (OptionDescriptor<?> option : owner.getOptions()) {
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
    for (ParameterDescriptor<?> parameter : getParameters()) {
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
  }

  public void printMan(Appendable writer) throws IOException {

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
    for (OptionDescriptor<?> option : owner.getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }
    if (printName) {
      writer.append(" ").append(getName());
    }
    for (OptionDescriptor<?> option : getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }
    for (ArgumentDescriptor<?> argument : getArguments()) {
      writer.append(" ");
      argument.printUsage(writer);
    }
    writer.append("\n\n");

    // Description
    String man = getDescription().getMan();
    if (man.length() > 0) {
      writer.append("DESCRIPTION\n");
      indent(Util.MAN_TAB, man, writer);
      writer.append("\n\n");
    }

    // Parameters
    List<OptionDescriptor<?>> options = new ArrayList<OptionDescriptor<?>>();
    options.addAll(owner.getOptions());
    options.addAll(getOptions());
    if (options.size() > 0) {
      writer.append("\nPARAMETERS\n");
      for (ParameterDescriptor<?> parameter : Util.join(owner.getOptions(), getParameters())) {
        writer.append(Util.MAN_TAB);
        parameter.printUsage(writer);
        String parameterText = parameter.getDescription().getBestEffortMan();
        if (parameterText.length() > 0) {
          writer.append("\n");
          indent(Util.MAN_TAB_EXTRA, parameterText, writer);
        }
        writer.append("\n\n");
      }
    }
  }
}
