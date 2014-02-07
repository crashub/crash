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

package org.crsh.cli.type;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for value types.
 */
public class ValueTypeFactory {

  /** A value type factory instance that provides a predefined set of value types. */
  public static final ValueTypeFactory DEFAULT = new ValueTypeFactory();

  /** The known types. */
  private final ValueType<?>[] types;

  private ValueTypeFactory() {
    this.types = new ValueType<?>[]{ ValueType.STRING, ValueType.INTEGER, ValueType.BOOLEAN,
        ValueType.ENUM, ValueType.PROPERTIES, ValueType.OBJECT_NAME, ValueType.THREAD, ValueType.FILE};
  }

  /**
   * Create a value type factory for the the default value types and the value types that the specified
   * classloader will load.
   *
   * @param loader the loader
   * @throws NullPointerException if the loader is null
   */
  public ValueTypeFactory(ClassLoader loader) throws NullPointerException {
    if (loader == null) {
      throw new NullPointerException("No null loader accepted");
    }

    //
    LinkedHashSet<ValueType> types = new LinkedHashSet<ValueType>();
    Collections.addAll(types, DEFAULT.types);
    Iterator<ValueType> sl = ServiceLoader.load(ValueType.class, loader).iterator();
    while (sl.hasNext()) {
      try {
        ValueType type = sl.next();
        types.add(type);
      }
      catch (ServiceConfigurationError e) {
        // Log it
        Logger logger = Logger.getLogger(ValueTypeFactory.class.getName());
        logger.log(Level.WARNING, "Could not load value type factory", e);
      }
    }

    //
    this.types = types.toArray(new ValueType[types.size()]);
  }

  public <T, S extends T> ValueType<T> get(Class<S> clazz) {
    ValueType<?> bestType = null;
    int bestDegree = Integer.MAX_VALUE;
    for (ValueType<?> type : types) {
      int degree = type.getDistance(clazz);
      if (degree != -1 && degree < bestDegree) {
        bestType = type;
        bestDegree = degree;
      }
    }
    return (ValueType<T>)bestType;
  }
}
