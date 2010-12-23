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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SimpleValueType<T> {

  /** . */
  public static final SimpleValueType<String> STRING = new SimpleValueType<String>(String.class) {
    @Override
    public String parse(String s) {
      return s;
    }
  };

  /** . */
  public static final SimpleValueType<Integer> INTEGER = new SimpleValueType<Integer>(Integer.class) {
    @Override
    public Integer parse(String s) {
      return Integer.parseInt(s);
    }
  };

  /** . */
  public static final SimpleValueType<Boolean> BOOLEAN = new SimpleValueType<Boolean>(Boolean.class) {
    @Override
    public Boolean parse(String s) {
      return Boolean.parseBoolean(s);
    }
  };

  /** . */
  private static final Map<Class<?>, SimpleValueType<?>> registry;

  static {
    Map<Class<?>, SimpleValueType<?>> tmp = new HashMap<Class<?>, SimpleValueType<?>>();
    tmp.put(String.class, STRING);
    tmp.put(Integer.class, INTEGER);
    tmp.put(Boolean.class, BOOLEAN);

    //
    registry = tmp;
  }

  public static SimpleValueType<?> get(Class<?> clazz) {
    return registry.get(clazz);
  }

  /** . */
  private final Class<T> javaType;

  private SimpleValueType(Class<T> javaType) {
    if (javaType == null) {
      throw new NullPointerException();
    }

    //
    this.javaType = javaType;
  }

  public Class<T> getJavaType() {
    return javaType;
  }

  public abstract T parse(String s);

}
