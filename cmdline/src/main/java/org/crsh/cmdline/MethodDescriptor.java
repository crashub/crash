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

import org.crsh.cmdline.binding.MethodArgumentBinding;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MethodDescriptor<T> extends CommandDescriptor<T, MethodArgumentBinding> {

  /** . */
  private final ClassDescriptor<T> owner;

  /** . */
  private final Method method;

  MethodDescriptor(
    ClassDescriptor<T> owner,
    Method method,
    String name,
    String description,
    List<ParameterDescriptor<MethodArgumentBinding>> parameters) throws IntrospectionException {
    super(name, description, parameters);

    //
    this.owner = owner;
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  @Override
  public Class<T> getType() {
    return owner.getType();
  }

  /** . */
  private static final String TAB = "       ";

  public void printUsage(PrintWriter writer) {
    writer.append("NAME\n");
    writer.append(TAB).append(owner.getName()).append(" ").append(getName());
    if (getDescription().length() > 0) {
      writer.append(" - ").append(getDescription());
    }
    writer.append("\n\n");

    //
    writer.append("SYNOPSIS\n");

    //
    writer.append(TAB).append(owner.getName());

    //
    for (OptionDescriptor<?> option : owner.getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }

    //
    writer.append(" ").append(getName());

    for (OptionDescriptor<?> option : getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }
    for (ArgumentDescriptor<?> argument : getArguments()) {
      writer.append(" ");
      argument.printUsage(writer);
    }
    writer.append("\n\n");

    //
    writer.append("DESCRIPTION\n");
    writer.append(TAB).append("The following options are available:\n\n");

    //
    for (OptionDescriptor<?> option : owner.getOptions()) {
      for (String name : option.getNames()) {
        writer.append(TAB).append(name.length() == 1 ? "-" : "--").append(name).append(TAB).append(option.getDescription()).append("\n\n");
      }
    }

    //
    for (OptionDescriptor<?> option : getOptions()) {
      for (String name : option.getNames()) {
        writer.append(TAB).append(name.length() == 1 ? "-" : "--").append(name).append(TAB).append(option.getDescription()).append("\n\n");
      }
    }
  }
}
