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

package org.crsh.command.introspector;

import org.crsh.command.Argument;
import org.crsh.command.Command;
import org.crsh.command.Description;
import org.crsh.command.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClassCommandInfo<T> extends CommandInfo<T> {

  /** . */
  private final Class<T> type;

  /** . */
  private final Map<String, MethodCommandInfo> commandMap;

  public ClassCommandInfo(Class<T> type) throws IntrospectionException {
    super(
      type.getSimpleName().toLowerCase(),
      description(type.getAnnotation(Description.class)),
      paremeters(type));

    //
    Map<String, MethodCommandInfo> commandMap = new HashMap<String, MethodCommandInfo>();
    for (MethodCommandInfo command : commands(type)) {
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

  public Iterable<MethodCommandInfo> getCommands() {
    return commandMap.values();
  }

  public MethodCommandInfo getCommand(String name) {
    return commandMap.get(name);
  }

  private static List<ParameterInfo> paremeters(Class<?> introspected) throws IntrospectionException {
    List<ParameterInfo> parameters;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      parameters = new ArrayList<ParameterInfo>();
    } else {
      parameters = paremeters(superIntrospected);
      for (Field f : introspected.getDeclaredFields()) {
        Description descriptionAnn = introspected.getAnnotation(Description.class);
        Argument argumentAnn = f.getAnnotation(Argument.class);
        Option optionAnn = f.getAnnotation(Option.class);
        ParameterInfo parameter = create(descriptionAnn, argumentAnn, optionAnn);
        if (parameter != null) {
          parameters.add(parameter);
        }
      }
    }
    return parameters;
  }

  private List<MethodCommandInfo<T>> commands(Class<?> introspected) throws IntrospectionException {
    List<MethodCommandInfo<T>> commands;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      commands = new ArrayList<MethodCommandInfo<T>>();
    } else {
      commands = commands(superIntrospected);
      for (Method m : introspected.getDeclaredMethods()) {
        Command command = m.getAnnotation(Command.class);
        if (command != null) {
          List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
          for (Annotation[] parameterAnnotations : m.getParameterAnnotations()) {
            Description descriptionAnn = null;
            Argument argumentAnn = null;
            Option optionAnn = null;
            for (Annotation parameterAnnotation : parameterAnnotations) {
              if (parameterAnnotation instanceof Option) {
                optionAnn = (Option)parameterAnnotation;
              } else if (parameterAnnotation instanceof Argument) {
                argumentAnn = (Argument)argumentAnn;
              } else if (parameterAnnotation instanceof Description) {
                descriptionAnn = (Description)parameterAnnotation;
              }
            }
            ParameterInfo parameter = create(descriptionAnn, argumentAnn, optionAnn);
            if (optionAnn != null) {
              parameters.add(parameter);
            } else {
              throw new IntrospectionException();
            }
          }
          Description descriptionAnn = m.getAnnotation(Description.class);
          commands.add(new MethodCommandInfo(this, m.getName().toLowerCase(), descriptionAnn != null ? descriptionAnn.value() : "", parameters));
        }
      }
    }
    return commands;
  }
}
