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

package org.crsh.cli.impl.lang;

import org.crsh.cli.Named;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Required;
import org.crsh.cli.type.ValueTypeFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CommandFactory {

  /** . */
  public static final CommandFactory DEFAULT = new CommandFactory();

  /** . */
  private static final Logger log = Logger.getLogger(CommandFactory.class.getName());

  /** . */
  protected final ValueTypeFactory valueTypeFactory;

  public CommandFactory() {
    this.valueTypeFactory = ValueTypeFactory.DEFAULT;
  }

  public CommandFactory(ClassLoader loader) throws NullPointerException {
    this(new ValueTypeFactory(loader));
  }

  public CommandFactory(ValueTypeFactory valueTypeFactory) throws NullPointerException {
    if (valueTypeFactory == null) {
      throw new NullPointerException("No null value type factory accepted");
    }

    //
    this.valueTypeFactory = valueTypeFactory;
  }


  private List<Method> findAllMethods(Class<?> introspected) throws IntrospectionException {
    List<Method> methods;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      methods = new ArrayList<Method>();
    } else {
      methods = findAllMethods(superIntrospected);
      for (Method method : introspected.getDeclaredMethods()) {
        if (method.getAnnotation(Command.class) != null) {
          methods.add(method);
        }
      }
    }
    return methods;
  }

  public <T> ObjectCommandDescriptor<T> create(Class<T> type) throws IntrospectionException {

    // Find all command methods
    List<Method> methods = findAllMethods(type);

    //
    String commandName;
    if (type.getAnnotation(Named.class) != null) {
      commandName = type.getAnnotation(Named.class).value();
    } else {
      commandName = type.getSimpleName();
    }

    //
    if (methods.size() == 1 && methods.get(0).getName().equals("main")) {
      MethodDescriptor<T> methodDescriptor = create(null, commandName, methods.get(0));
      for (ParameterDescriptor parameter : parameters(type)) {
        methodDescriptor.addParameter(parameter);
      }
      return methodDescriptor;
    } else {
      Map<String, MethodDescriptor<T>> methodMap = new LinkedHashMap<String, MethodDescriptor<T>>();
      ClassDescriptor<T> classDescriptor = new ClassDescriptor<T>(type, commandName, methodMap, new Description(type));
      for (Method method : methods) {
        String methodName;
        if (method.getAnnotation(Named.class) != null) {
          methodName = method.getAnnotation(Named.class).value();
        } else {
          methodName = method.getName();
        }
        MethodDescriptor<T> methodDescriptor = create(classDescriptor, methodName, method);
        methodMap.put(methodDescriptor.getName(), methodDescriptor);
      }
      for (ParameterDescriptor parameter : parameters(type)) {
        classDescriptor.addParameter(parameter);
      }
      return classDescriptor;
    }
  }

  private <T> MethodDescriptor<T> create(ClassDescriptor<T> classDescriptor, String name, Method method) throws IntrospectionException {
    Description info = new Description(method);
    MethodDescriptor<T> methodDescriptor = new MethodDescriptor<T>(
        classDescriptor,
        method,
        name,
        info);

    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotationMatrix = method.getParameterAnnotations();
    for (int i = 0;i < parameterAnnotationMatrix.length;i++) {

      Annotation[] parameterAnnotations = parameterAnnotationMatrix[i];
      Type parameterType = parameterTypes[i];
      Tuple tuple = get(parameterAnnotations);

      MethodArgumentBinding binding = new MethodArgumentBinding(i);
      ParameterDescriptor parameter = create(
          binding,
          parameterType,
          tuple.argumentAnn,
          tuple.optionAnn,
          tuple.required,
          tuple.descriptionAnn,
          tuple.ann);
      if (parameter != null) {
        methodDescriptor.addParameter(parameter);
      } else {
        log.log(Level.FINE, "Method argument with index " + i + " of method " + method + " is not annotated");
      }
    }
    return methodDescriptor;
  }

  private ParameterDescriptor create(
      Binding binding,
      Type type,
      Argument argumentAnn,
      Option optionAnn,
      boolean required,
      Description info,
      Annotation ann) throws IntrospectionException {

    //
    if (argumentAnn != null) {
      if (optionAnn != null) {
        throw new IntrospectionException();
      }

      //
      return new BoundArgumentDescriptor(
          binding,
          argumentAnn.name(),
          ParameterType.create(valueTypeFactory, type),
          info,
          required,
          false,
          argumentAnn.unquote(),
          argumentAnn.completer(),
          ann);
    } else if (optionAnn != null) {
      return new BoundOptionDescriptor(
          binding,
          ParameterType.create(valueTypeFactory, type),
          Collections.unmodifiableList(Arrays.asList(optionAnn.names())),
          info,
          required,
          false,
          optionAnn.unquote(),
          optionAnn.completer(),
          ann);
    } else {
      return null;
    }
  }

  private static Tuple get(Annotation... ab) {
    Argument argumentAnn = null;
    Option optionAnn = null;
    Boolean required = null;
    Description description = new Description(ab);
    Annotation info = null;
    for (Annotation parameterAnnotation : ab) {
      if (parameterAnnotation instanceof Option) {
        optionAnn = (Option)parameterAnnotation;
      } else if (parameterAnnotation instanceof Argument) {
        argumentAnn = (Argument)parameterAnnotation;
      } else if (parameterAnnotation instanceof Required) {
        required = ((Required)parameterAnnotation).value();
      } else if (info == null) {

        // Look at annotated annotations
        Class<? extends Annotation> a = parameterAnnotation.annotationType();
        if (a.getAnnotation(Option.class) != null) {
          optionAnn = a.getAnnotation(Option.class);
          info = parameterAnnotation;
        } else if (a.getAnnotation(Argument.class) != null) {
          argumentAnn =  a.getAnnotation(Argument.class);
          info = parameterAnnotation;
        }

        //
        if (info != null) {

          //
          description = new Description(description, new Description(a));

          //
          if (required == null) {
            Required metaReq = a.getAnnotation(Required.class);
            if (metaReq != null) {
              required = metaReq.value();
            }
          }
        }
      }
    }

    //
    return new Tuple(argumentAnn, optionAnn, required != null && required,description, info);
  }

  /**
   * Jus grouping some data for conveniency
   */
  protected static class Tuple {
    final Argument argumentAnn;
    final Option optionAnn;
    final boolean required;
    final Description descriptionAnn;
    final Annotation ann;
    private Tuple(Argument argumentAnn, Option optionAnn, boolean required, Description info, Annotation ann) {
      this.argumentAnn = argumentAnn;
      this.optionAnn = optionAnn;
      this.required = required;
      this.descriptionAnn = info;
      this.ann = ann;
    }
  }

  private List<ParameterDescriptor> parameters(Class<?> introspected) throws IntrospectionException {
    List<ParameterDescriptor> parameters;
    Class<?> superIntrospected = introspected.getSuperclass();
    if (superIntrospected == null) {
      parameters = new ArrayList<ParameterDescriptor>();
    } else {
      parameters = parameters(superIntrospected);
      for (Field f : introspected.getDeclaredFields()) {
        Tuple tuple = get(f.getAnnotations());
        ClassFieldBinding binding = new ClassFieldBinding(f);
        ParameterDescriptor parameter = create(
            binding,
            f.getGenericType(),
            tuple.argumentAnn,
            tuple.optionAnn,
            tuple.required,
            tuple.descriptionAnn,
            tuple.ann);
        if (parameter != null) {
          parameters.add(parameter);
        }
      }
    }
    return parameters;
  }
}
