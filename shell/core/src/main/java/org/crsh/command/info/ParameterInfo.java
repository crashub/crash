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

package org.crsh.command.info;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ParameterInfo<J extends JoinPoint> {

  /** . */
  private final J joinPoint;

  /** . */
  private final String description;

  /** . */
  private final ValueType type;

  /** . */
  private final boolean required;

  /** . */
  private final boolean password;

  /** . */
  private final Type javaType;

  public ParameterInfo(
    J joinPoint,
    Type javaType,
    String description,
    boolean required,
    boolean password) throws IllegalValueTypeException, IllegalParameterException {

    //
    this.joinPoint = joinPoint;
    this.javaType = javaType;
    this.description = description;
    this.type = create(javaType);
    this.required = required;
    this.password = password;
  }

  public J getJoinPoint() {
    return joinPoint;
  }

  public String getDescription() {
    return description;
  }

  public ValueType getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isPassword() {
    return password;
  }

  private ValueType create(Type type) throws IllegalValueTypeException {

    Class<?> classType;
    Multiplicity multiplicity;
    if (type instanceof Class<?>) {
      classType = (Class<Object>)type;
      multiplicity = Multiplicity.SINGLE;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<Object>)rawType;
        if (List.class.equals(classRawType)) {
          Type elementType = parameterizedType.getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            classType = (Class<Object>)elementType;
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
    if (classType == String.class) {
      valueType = SimpleValueType.STRING;
    } else if (classType == Integer.class || classType == int.class) {
      valueType = SimpleValueType.INTEGER;
    } else if (classType == Boolean.class || classType == boolean.class) {
      valueType = SimpleValueType.BOOLEAN;
    } else {
      throw new IllegalValueTypeException();
    }

    //
    return new ValueType(valueType, multiplicity);
  }
}
