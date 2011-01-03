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

import org.crsh.cmdline.spi.Completer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SimpleValueType<T> {

  /** . */
  public static final SimpleValueType<String> STRING = new SimpleValueType<String>(String.class, EmptyCompleter.class) {
    @Override
    public <S extends String> String parse(Class<S> type, String s) {
      return s;
    }
  };

  /** . */
  public static final SimpleValueType<Integer> INTEGER = new SimpleValueType<Integer>(Integer.class, EmptyCompleter.class) {
    @Override
    public <S extends Integer> Integer parse(Class<S> type, String s) {
      return Integer.parseInt(s);
    }
  };

  /** . */
  public static final SimpleValueType<Boolean> BOOLEAN = new SimpleValueType<Boolean>(Boolean.class, EmptyCompleter.class) {
    @Override
    public <S extends Boolean> Boolean parse(Class<S> type, String s) {
      return Boolean.parseBoolean(s);
    }
  };

  /** . */
  public static final SimpleValueType<Enum> ENUM = new SimpleValueType<Enum>(Enum.class, EnumCompleter.class) {
    @Override
    public <S extends Enum> Enum parse(Class<S> type, String s) {
      return Enum.valueOf(type, s);
    }
  };

  /** . */
  private static final SimpleValueType<?>[] types = { STRING, INTEGER, BOOLEAN, ENUM};

  public static SimpleValueType<?> get(Class<?> clazz) {
    SimpleValueType<?> bestType = null;
    int bestDegree = Integer.MAX_VALUE;
    for (SimpleValueType<?> type : types) {
      int degree = type.getRelativeDegree(clazz);
      if (degree != -1 && degree < bestDegree) {
        bestType = type;
        bestDegree = degree;
      }
    }
    return bestType;
  }

  /** . */
  private final Class<T> javaType;

  /** . */
  private final Class<? extends Completer> completer;

  private SimpleValueType(Class<T> javaType, Class<? extends Completer> completer) {
    if (javaType == null) {
      throw new NullPointerException();
    }

    //
    this.completer = completer;
    this.javaType = javaType;
  }

  public int getRelativeDegree(Class<?> clazz) {
    if (javaType == clazz) {
      return 0;
    } else if (javaType.isAssignableFrom(clazz)) {
      int degree = 0;
      for (Class<?> current = clazz;current != javaType;current = current.getSuperclass()) {
        degree++;
      }
      return degree;
    } else {
      return -1;
    }
  }

  public Class<? extends Completer> getCompleter() {
    return completer;
  }

  public Class<T> getJavaType() {
    return javaType;
  }

  public abstract <S extends T> T parse(Class<S> type, String s);

}
