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

import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.spi.Completer;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ParameterDescriptor<B extends TypeBinding> {

  /** . */
  private final B binding;

  /** . */
  private final String description;

  /** . */
  private final SimpleValueType type;

  /** . */
  private final Multiplicity multiplicity;

  /** . */
  private final boolean required;

  /** . */
  private final boolean password;

  /** . */
  private final Type javaType;

  /** . */
  private final Class<?> javaComponentType;

  /** . */
  private final Class<? extends Completer> completerType;

  /** The annotation when it exists.  */
  private final Annotation annotation;

  public ParameterDescriptor(
    B binding,
    Type javaType,
    String description,
    boolean required,
    boolean password,
    Class<? extends Completer> completerType,
    Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {

    Class<?> javaComponentType;
    Multiplicity multiplicity;
    if (javaType instanceof Class<?>) {
      javaComponentType = (Class<Object>)javaType;
      multiplicity = Multiplicity.SINGLE;
    } else if (javaType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)javaType;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<Object>)rawType;
        if (List.class.equals(classRawType)) {
          Type elementType = parameterizedType.getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            javaComponentType = (Class<Object>)elementType;
            multiplicity = Multiplicity.LIST;
          } else {
            throw new IllegalValueTypeException();
          }
        } else {
          throw new IllegalValueTypeException();
        }
      } else {
        throw new IllegalValueTypeException();
      }
    } else {
      throw new IllegalValueTypeException();
    }

    //
    SimpleValueType valueType;
    if (javaComponentType == String.class) {
      valueType = SimpleValueType.STRING;
    } else if (javaComponentType == Integer.class || javaComponentType == int.class) {
      valueType = SimpleValueType.INTEGER;
    } else if (javaComponentType == Boolean.class || javaComponentType == boolean.class) {
      valueType = SimpleValueType.BOOLEAN;
    } else if (Enum.class.isAssignableFrom(javaComponentType)) {
      valueType = SimpleValueType.ENUM;
    } else {
      throw new IllegalValueTypeException();
    }

    //
    this.binding = binding;
    this.javaType = javaType;
    this.description = description;
    this.type = valueType;
    this.multiplicity = multiplicity;
    this.required = required;
    this.password = password;
    this.completerType = completerType;
    this.annotation = annotation;
    this.javaComponentType = javaComponentType;
  }

  public Object parse(String s) {
    return type.parse(javaComponentType, s);
  }

  public final B getBinding() {
    return binding;
  }

  public final String getDescription() {
    return description;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public final boolean isRequired() {
    return required;
  }

  public final boolean isPassword() {
    return password;
  }

  public final SimpleValueType getType() {
    return type;
  }

  public final Multiplicity getMultiplicity() {
    return multiplicity;
  }

  public final Class<? extends Completer> getCompleterType() {
    return completerType;
  }
}