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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MethodDescriptor<T> extends CommandDescriptor<T, MethodArgumentBinding> {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(MethodDescriptor.class);

  public static <T> MethodDescriptor<T> create(ClassDescriptor<T> owner, Method m) throws IntrospectionException {
    Command command = m.getAnnotation(Command.class);
    if (command != null) {
      List<ParameterDescriptor<MethodArgumentBinding>> parameters = new ArrayList<ParameterDescriptor<MethodArgumentBinding>>();
      Type[] parameterTypes = m.getGenericParameterTypes();
      Annotation[][] parameterAnnotationMatrix = m.getParameterAnnotations();
      for (int i = 0;i < parameterAnnotationMatrix.length;i++) {


        Annotation[] parameterAnnotations = parameterAnnotationMatrix[i];
        Type parameterType = parameterTypes[i];
        Tuple tuple = get(parameterAnnotations);


        MethodArgumentBinding binding = new MethodArgumentBinding(i);
        ParameterDescriptor<MethodArgumentBinding> parameter = create(
          binding,
          parameterType,
          tuple.argumentAnn,
          tuple.optionAnn,
          tuple.descriptionAnn,
          tuple.ann);
        if (parameter != null) {
          parameters.add(parameter);
        } else {
          log.debug("Method argument with index " + i + " of method " + m + " is not annotated");
        }
      }

      //
      InfoDescriptor info = new InfoDescriptor(m);

      //
      return new MethodDescriptor<T>(
        owner,
        m,
        m.getName().toLowerCase(),
        info,
        parameters);
    } else {
      return null;
    }
  }

  /** . */
  private final ClassDescriptor<T> owner;

  /** . */
  private final Method method;

  /** . */
  private final int size;

  /** . */
  private final Map<Integer, ParameterDescriptor<MethodArgumentBinding>> parameterMap;

  MethodDescriptor(
    ClassDescriptor<T> owner,
    Method method,
    String name,
    InfoDescriptor info,
    List<ParameterDescriptor<MethodArgumentBinding>> parameters) throws IntrospectionException {
    super(name, info, parameters);

    Map<Integer, ParameterDescriptor<MethodArgumentBinding>> parameterMap = new HashMap<Integer, ParameterDescriptor<MethodArgumentBinding>>();
    for (ParameterDescriptor<MethodArgumentBinding> parameter : parameters) {
      parameterMap.put(parameter.getBinding().getIndex(), parameter);
    }

    //
    this.owner = owner;
    this.method = method;
    this.size = method.getParameterTypes().length;
    this.parameterMap = parameterMap;
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
    return parameterMap.get(index);
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

  @Override
  public void printUsage(PrintWriter writer) {
    printMan(writer, true);
  }

  public void printUsage(PrintWriter writer, boolean printName) {

    writer.append("usage: ").append(owner.getName());
    if (printName) {
      writer.append(" ").append(getName());
    }

    //
    for (OptionDescriptor<?> option : owner.getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }
    if (printName) {
      writer.append(" ").append(getName());
    }

    //
    for (OptionDescriptor<?> option : getOptions()) {
      writer.append(" ");
      option.printUsage(writer);
    }
    for (ArgumentDescriptor<?> argument : getArguments()) {
      writer.append(" ");
      argument.printUsage(writer);
    }

    //
    writer.append("\n\n");

    //
    for (OptionDescriptor<?> option : owner.getOptions()) {
      writer.append(TAB);
      option.printUsage(writer);
      writer.append(" ");
      writer.append(option.getDescription());
    }
    for (OptionDescriptor<?> option : getOptions()) {
      writer.append(TAB);
      option.printUsage(writer);
      writer.append(" ");
      writer.append(option.getDescription());
    }
    for (ArgumentDescriptor<?> argument : getArguments()) {
      writer.append(TAB);
      argument.printUsage(writer);
      writer.append(" ");
      writer.append(argument.getDescription());
    }

    //
    writer.append("\n\n");
  }

  public void printMan(PrintWriter writer) {
    printMan(writer, true);
  }

  void printMan(PrintWriter writer, boolean printName) {
    writer.append("NAME\n");
    writer.append(TAB).append(owner.getName());
    if (printName) {
      writer.append(" ").append(getName());
    }
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
    if (printName) {
      writer.append(" ").append(getName());
    }

    //
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
