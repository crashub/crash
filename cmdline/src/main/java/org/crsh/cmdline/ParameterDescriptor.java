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
  private final InfoDescriptor info;

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
  private final Class<?> javaValueType;

  /** . */
  private final Class<? extends Completer> completerType;

  /** The annotation when it exists.  */
  private final Annotation annotation;

  public ParameterDescriptor(
    B binding,
    Type javaType,
    InfoDescriptor info,
    boolean required,
    boolean password,
    Class<? extends Completer> completerType,
    Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {

    //
    Class<?> javaValueType;
    Multiplicity multiplicity;
    if (javaType instanceof Class<?>) {
      javaValueType = (Class<Object>)javaType;
      multiplicity = Multiplicity.SINGLE;
    } else if (javaType instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)javaType;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<Object>)rawType;
        if (List.class.equals(classRawType)) {
          Type elementType = parameterizedType.getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            javaValueType = (Class<Object>)elementType;
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
    if (javaValueType == String.class) {
      valueType = SimpleValueType.STRING;
    } else if (javaValueType == Integer.class || javaValueType == int.class) {
      valueType = SimpleValueType.INTEGER;
    } else if (javaValueType == Boolean.class || javaValueType == boolean.class) {
      valueType = SimpleValueType.BOOLEAN;
    } else if (Enum.class.isAssignableFrom(javaValueType)) {
      valueType = SimpleValueType.ENUM;
    } else {
      throw new IllegalValueTypeException();
    }

    //
    if (completerType == EmptyCompleter.class) {
      completerType = valueType.getCompleter();
    }

    // Make it required if it's a primitive
    required |= javaValueType.isPrimitive();

    //
    this.binding = binding;
    this.javaType = javaType;
    this.info = info;
    this.type = valueType;
    this.multiplicity = multiplicity;
    this.required = required;
    this.password = password;
    this.completerType = completerType;
    this.annotation = annotation;
    this.javaValueType = javaValueType;
  }

  public Object parse(String s) {
    return type.parse(javaValueType, s);
  }

  public Type getJavaType() {
    return javaType;
  }

  public Class<?> getJavaValueType() {
    return javaValueType;
  }

  public final B getBinding() {
    return binding;
  }

  public final String getDescription() {
    return info != null ? info.getDisplay() : "";
  }

  public InfoDescriptor getInfo() {
    return info;
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