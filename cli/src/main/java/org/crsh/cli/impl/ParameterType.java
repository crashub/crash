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

package org.crsh.cli.impl;

import org.crsh.cli.impl.descriptor.IllegalValueTypeException;
import org.crsh.cli.type.ValueType;
import org.crsh.cli.type.ValueTypeFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public final class ParameterType<V> {

  public static ParameterType create(ValueTypeFactory factory, Type type) throws IllegalValueTypeException {
    Class<?> declaredType;
    Multiplicity multiplicity;
    if (type instanceof Class<?>) {
      declaredType = (Class<Object>)type;
      multiplicity = Multiplicity.SINGLE;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<Object>)rawType;
        if (List.class.equals(classRawType)) {
          Type elementType = parameterizedType.getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            declaredType = (Class<Object>)elementType;
            multiplicity = Multiplicity.MULTI;
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
    Class<?> effectiveType;
    ValueType valueType;
    if (declaredType == String.class) {
      effectiveType = String.class;
      valueType = ValueType.STRING;
    } else if (declaredType == Integer.class || declaredType == int.class) {
      effectiveType = Integer.class;
      valueType = ValueType.INTEGER;
    } else if (declaredType == Boolean.class || declaredType == boolean.class) {
      effectiveType = Boolean.class;
      valueType = ValueType.BOOLEAN;
    } else if (Enum.class.isAssignableFrom(declaredType)) {
      effectiveType = declaredType;
      valueType = ValueType.ENUM;
    } else {
      effectiveType = declaredType;
      valueType = factory.get(declaredType);
      if (valueType == null) {
        throw new IllegalValueTypeException("Type " + declaredType.getName() + " is not handled at the moment");
      }
    }

    //
    return new ParameterType(multiplicity, declaredType, effectiveType, valueType);
  }

  /** . */
  private final Multiplicity multiplicity;

  /** . */
  private final Class<?> declaredType;

  /** . */
  private final Class<V> effectiveType;

  /** . */
  private final ValueType<V> valueType;

  ParameterType(Multiplicity multiplicity, Class<?> declaredType, Class<V> effectiveType, ValueType<V> valueType) {
    this.multiplicity = multiplicity;
    this.declaredType = declaredType;
    this.effectiveType = effectiveType;
    this.valueType = valueType;
  }

  public Object parse(String s) throws Exception {
    return valueType.parse(effectiveType, s);
  }

  public Multiplicity getMultiplicity() {
    return multiplicity;
  }

  public Class<?> getDeclaredType() {
    return declaredType;
  }

  public Class<V> getEffectiveType() {
    return effectiveType;
  }

  public ValueType<V> getValueType() {
    return valueType;
  }
}
