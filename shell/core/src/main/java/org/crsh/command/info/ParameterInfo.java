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
public abstract class ParameterInfo {

  /** . */
  private final String description;

  /** . */
  private final ParameterType type;

  /** . */
  private final boolean required;

  /** . */
  private final boolean password;

  /** . */
  private final Type javaType;

  public ParameterInfo(
    Type javaType,
    String description,
    boolean required,
    boolean password) throws IllegalParameterTypeException {

    //
    this.javaType = javaType;
    this.description = description;
    this.type = create(javaType);
    this.required = required;
    this.password = password;
  }

  public String getDescription() {
    return description;
  }

  public ParameterType getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

  public boolean isPassword() {
    return password;
  }

  private ParameterType create(Type type) throws IllegalParameterTypeException {

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
            throw new IllegalParameterTypeException();
          }
        } else {
          throw new IllegalParameterTypeException();
        }
      } else {
        throw new IllegalParameterTypeException();
      }
    } else {
      throw new IllegalParameterTypeException();
    }

    //
    ValueType valueType;
    if (classType == String.class) {
      valueType = ValueType.STRING;
    } else if (classType == Integer.class || classType == int.class) {
      valueType = ValueType.INTEGER;
    } else if (classType == Boolean.class || classType == boolean.class) {
      valueType = ValueType.BOOLEAN;
    } else {
      throw new IllegalParameterTypeException();
    }

    //
    return new ParameterType(valueType, multiplicity);
  }
}
