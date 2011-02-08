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

import org.crsh.cmdline.binding.ClassFieldBinding;
import static org.crsh.cmdline.Util.indent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A command backed by a class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClassDescriptor<T> extends CommandDescriptor<T, ClassFieldBinding> {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(ClassDescriptor.class);

  /** . */
  private final Class<T> type;

  /** . */
  private final Map<String, MethodDescriptor<T>> methodMap;

  public ClassDescriptor(Class<T> type, Description info) throws IntrospectionException {
    super(type.getSimpleName().toLowerCase(), info);

    // Make sure we can add it
    Map<String, MethodDescriptor<T>> methodMap = new LinkedHashMap<String, MethodDescriptor<T>>();
    for (MethodDescriptor<T> method : commands(type)) {
      //
      methodMap.put(method.getName(), method);
    }

    //
    this.methodMap = methodMap;
    this.type = type;
  }

  @Override
  void addParameter(ParameterDescriptor<ClassFieldBinding> parameter) throws IntrospectionException {

    // Check we can add the option
    if (parameter instanceof OptionDescriptor<?>) {
      OptionDescriptor<ClassFieldBinding> option = (OptionDescriptor<ClassFieldBinding>)parameter;
      Set<String> blah = new HashSet<String>();
      for (String optionName : option.getNames()) {
        blah.add((optionName.length() == 1 ? "-" : "--") + optionName);
      }
      for (MethodDescriptor<T> method : methodMap.values()) {
        Set<String> diff = new HashSet<String>(method.getOptionNames());
        diff.retainAll(blah);
        if (diff.size() > 0) {
          throw new IntrospectionException("Cannot add method " + method.getName() + " because it has common "
          + " options with its class: " + diff);
        }
      }
    }

    //
    super.addParameter(parameter);
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public Map<String, ? extends CommandDescriptor<T, ?>> getSubordinates() {
    return methodMap;
  }

  @Override
  public OptionDescriptor<?> findOption(String name) {
    return getOption(name);
  }

  @Override
  public void printUsage(Appendable writer) throws IOException {
    if (methodMap.size() == 1) {
      methodMap.values().iterator().next().printUsage(writer);
    } else {
      writer.append("usage: ").append(getName());
      for (OptionDescriptor<?> option : getOptions()) {
        option.printUsage(writer);
      }
      writer.append(" COMMAND [ARGS]\n\n");
      writer.append("The most commonly used ").append(getName()).append(" commands are:\n");
      String format = "   %1$-16s %2$s\n";
      for (MethodDescriptor<T> method : getMethods()) {
        Formatter formatter = new Formatter(writer);
        formatter.format(format, method.getName(), method.getUsage());
      }
    }
  }

  public void printMan(Appendable writer) throws IOException {
    if (methodMap.size() == 1) {
      methodMap.values().iterator().next().printMan(writer);
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
      for (OptionDescriptor<?> option : getOptions()) {
        writer.append(" ");
        option.printUsage(writer);
      }
      writer.append(" COMMAND [ARGS]\n\n");

      //
      String man = getDescription().getMan();
      if (man.length() > 0) {
        writer.append("DESCRIPTION\n");
        indent(Util.MAN_TAB, man, writer);
        writer.append("\n\n");
      }

      // Common options
      if (getOptions().size() > 0) {
        writer.append("PARAMETERS\n");
        for (OptionDescriptor<?> option : getOptions()) {
          writer.append(Util.MAN_TAB);
          option.printUsage(writer);
          String optionText = option.getDescription().getBestEffortMan();
          if (optionText.length() > 0) {
            writer.append("\n");
            indent(Util.MAN_TAB_EXTRA, optionText, writer);
          }
          writer.append("\n\n");
        }
      }

      //
      writer.append("COMMANDS\n");
      for (MethodDescriptor<T> method : getMethods()) {
        writer.append(Util.MAN_TAB).append(method.getName());
        String methodText = method.getDescription().getBestEffortMan();
        if (methodText.length() > 0) {
          writer.append("\n");
          indent(Util.MAN_TAB_EXTRA, methodText, writer);
        }
        writer.append("\n\n");
      }
    }
  }

  public Iterable<MethodDescriptor<T>> getMethods() {
    return methodMap.values();
  }

  public MethodDescriptor<T> getMethod(String name) {
    return methodMap.get(name);
  }

  private List<MethodDescriptor<T>> commands(Class<?> introspected) throws IntrospectionException {
    List<MethodDescriptor<T>> commands;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      commands = new ArrayList<MethodDescriptor<T>>();
    } else {
      commands = commands(superIntrospected);
      for (Method m : introspected.getDeclaredMethods()) {
        MethodDescriptor<T> mDesc = CommandFactory.create(this, m);
        if (mDesc != null) {
          commands.add(mDesc);
        }
      }
    }
    return commands;
  }
}
