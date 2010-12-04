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

package org.crsh.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TypeResolver {

  /**
   * A simplistic implementation, it may not handle all cases but it should handle enough.
   *
   * @param implementation the type for which the parameter requires a resolution
   * @param type the type that owns the parameter
   * @param parameterIndex the parameter index
   * @return the resolved type
   */
  public static Type resolve(Type implementation, Class<?> type, int parameterIndex) {
    if (implementation == null) {
      throw new NullPointerException();
    }

    //
    if (implementation == type) {
      TypeVariable<? extends Class<?>>[] tp = type.getTypeParameters();
      if (parameterIndex < tp.length) {
        return tp[parameterIndex];
      } else {
        throw new IllegalArgumentException();
      }
    } else if (implementation instanceof Class<?>) {
      Class<?> c = (Class<?>) implementation;
      Type gsc = c.getGenericSuperclass();
      Type resolved = null;
      if (gsc != null) {
        resolved = resolve(gsc, type, parameterIndex);
        if (resolved == null) {
          // Try with interface
        }
      }
      return resolved;
    } else if (implementation instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) implementation;
      Type[] typeArgs = pt.getActualTypeArguments();
      Type rawType = pt.getRawType();
      if (rawType == type) {
        return typeArgs[parameterIndex];
      } else if (rawType instanceof Class<?>) {
        Class<?> classRawType = (Class<?>)rawType;
        Type resolved = resolve(classRawType, type, parameterIndex);
        if (resolved == null) {
          return null;
        } else if (resolved instanceof TypeVariable) {
          TypeVariable resolvedTV = (TypeVariable)resolved;
          TypeVariable[] a = classRawType.getTypeParameters();
          for (int i = 0;i < a.length;i++) {
            if (a[i].equals(resolvedTV)) {
              return resolve(implementation, classRawType, i);
            }
          }
          throw new AssertionError();
        } else {
          throw new UnsupportedOperationException("Cannot support resolution of " + resolved);
        }
      } else {
        throw new UnsupportedOperationException();
      }
    } else {
      throw new UnsupportedOperationException("todo " + implementation + " " + implementation.getClass());
    }
  }
}
