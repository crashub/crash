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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
public class ClassDescriptor<T> extends CommandDescriptor<T, ParameterBinding.ClassField> {

  /** . */
  private final Class<T> type;

  /** . */
  private final Map<String, MethodDescriptor<T>> commandMap;

  public ClassDescriptor(Class<T> type) throws IntrospectionException {
    super(
      type.getSimpleName().toLowerCase(),
      description(type.getAnnotation(Description.class)),
      parameters(type));

    //
    Map<String, MethodDescriptor<T>> commandMap = new HashMap<String, MethodDescriptor<T>>();
    for (MethodDescriptor<T> command : commands(type)) {
      commandMap.put(command.getName(), command);
    }

    //
    this.commandMap = commandMap;
    this.type = type;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  public Iterable<MethodDescriptor<T>> getCommands() {
    return commandMap.values();
  }

  public MethodDescriptor<T> getMethod(String name) {
    return commandMap.get(name);
  }

  /** . */
  private static final String TAB = "       ";

  public String getUsage() {
    StringBuilder sb = new StringBuilder();

    //
    sb.append("NAME\n");
    sb.append(TAB).append(type.getSimpleName());
    if (getDescription().length() > 0) {
      sb.append(" - ").append(getDescription());
    }
    sb.append("\n\n");

    //
    sb.append("SYNOPSIS\n");
    sb.append(TAB).append(type.getSimpleName());
    for (OptionDescriptor<ParameterBinding.ClassField> option : getOptions()) {
      sb.append(" [");
      boolean a = false;
      for (String name : option.getNames()) {
        if (a) {
          sb.append(" | ");
        }
        sb.append(name.length() == 1 ? "-" : "--").append(name);
        a = true;
      }
      sb.append("]");
    }
    for (ArgumentDescriptor<ParameterBinding.ClassField> argument : getArguments()) {
      if (argument.getType().getMultiplicity() == Multiplicity.SINGLE) {
        sb.append(" ...");
      } else {
        sb.append(" arg");
      }
    }
    sb.append("\n\n");

    //
    sb.append("DESCRIPTION\n");
    sb.append(TAB).append("The following options are available:\n\n");
    for (OptionDescriptor<ParameterBinding.ClassField> option : getOptions()) {
      for (String name : option.getNames()) {
        sb.append(TAB).append(name.length() == 1 ? "-" : "--").append(name).append(TAB).append(option.getDescription()).append("\n\n");
      }
    }

    //
    return sb.toString();
  }


  private static List<ParameterDescriptor<ParameterBinding.ClassField>> parameters(Class<?> introspected) throws IntrospectionException {
    List<ParameterDescriptor<ParameterBinding.ClassField>> parameters;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      parameters = new ArrayList<ParameterDescriptor<ParameterBinding.ClassField>>();
    } else {
      parameters = parameters(superIntrospected);
      for (Field f : introspected.getDeclaredFields()) {
        Description descriptionAnn = f.getAnnotation(Description.class);
        Argument argumentAnn = f.getAnnotation(Argument.class);
        Option optionAnn = f.getAnnotation(Option.class);
        ParameterBinding.ClassField binding = new ParameterBinding.ClassField(f);
        ParameterDescriptor<ParameterBinding.ClassField> parameter = create(binding, f.getGenericType(), descriptionAnn, argumentAnn, optionAnn);
        if (parameter != null) {
          parameters.add(parameter);
        }
      }
    }
    return parameters;
  }

  private List<MethodDescriptor<T>> commands(Class<?> introspected) throws IntrospectionException {
    List<MethodDescriptor<T>> commands;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      commands = new ArrayList<MethodDescriptor<T>>();
    } else {
      commands = commands(superIntrospected);
      for (Method m : introspected.getDeclaredMethods()) {
        Command command = m.getAnnotation(Command.class);
        if (command != null) {
          List<ParameterDescriptor<ParameterBinding.MethodArgument>> parameters = new ArrayList<ParameterDescriptor<ParameterBinding.MethodArgument>>();
          Type[] parameterTypes = m.getGenericParameterTypes();
          Annotation[][] parameterAnnotationMatrix = m.getParameterAnnotations();
          for (int i = 0;i < parameterAnnotationMatrix.length;i++) {
            Annotation[] parameterAnnotations = parameterAnnotationMatrix[i];
            Type parameterType = parameterTypes[i];
            Description descriptionAnn = null;
            Argument argumentAnn = null;
            Option optionAnn = null;
            for (Annotation parameterAnnotation : parameterAnnotations) {
              if (parameterAnnotation instanceof Option) {
                optionAnn = (Option)parameterAnnotation;
              } else if (parameterAnnotation instanceof Argument) {
                argumentAnn = (Argument)parameterAnnotation;
              } else if (parameterAnnotation instanceof Description) {
                descriptionAnn = (Description)parameterAnnotation;
              }
            }
            ParameterBinding.MethodArgument binding = new ParameterBinding.MethodArgument(m, i);
            ParameterDescriptor<ParameterBinding.MethodArgument> parameter = create(binding, parameterType, descriptionAnn, argumentAnn, optionAnn);
            if (optionAnn != null) {
              parameters.add(parameter);
            } else {
              throw new IntrospectionException();
            }
          }
          Description descriptionAnn = m.getAnnotation(Description.class);
          commands.add(new MethodDescriptor<T>(
            this,
            m.getName().toLowerCase(),
            descriptionAnn != null ? descriptionAnn.value() : "",
            parameters));
        }
      }
    }
    return commands;
  }
}
