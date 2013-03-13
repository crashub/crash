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

import org.crsh.cli.completers.EmptyCompleter;
import org.crsh.cli.completers.EnumCompleter;
import org.crsh.cli.spi.Completer;

import javax.management.ObjectName;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * A type for values.
 *
 * @param <V> the generic value type
 */
public abstract class ValueType<V> {

  /** . */
  public static final ValueType<String> STRING = new ValueType<String>(String.class) {
    @Override
    public <S extends String> S parse(Class<S> type, String s) throws Exception {
      return type.cast(s);
    }
  };

  /** . */
  public static final ValueType<Integer> INTEGER = new ValueType<Integer>(Integer.class) {
    @Override
    public <S extends Integer> S parse(Class<S> type, String s) throws Exception {
      return type.cast(Integer.parseInt(s));
    }
  };

  /** . */
  public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>(Boolean.class) {
    @Override
    public <S extends Boolean> S parse(Class<S> type, String s) throws Exception {
      return type.cast(Boolean.parseBoolean(s));
    }
  };

  /** . */
  public static final ValueType<Enum> ENUM = new ValueType<Enum>(Enum.class, EnumCompleter.class) {
    @Override
    public <S extends Enum> S parse(Class<S> type, String s) {
      // We cannot express S extends Enum<S> type
      // so we need this necessary cast to make the java compiler happy
      S s1 = (S)Enum.valueOf(type, s);
      return s1;
    }
  };

  /** . */
  public static final ValueType<Properties> PROPERTIES = new ValueType<Properties>(Properties.class) {
    @Override
    public <S extends Properties> S parse(Class<S> type, String s) throws Exception {
      java.util.Properties props = new java.util.Properties();
      StringTokenizer tokenizer = new StringTokenizer(s, ";", false);
      while(tokenizer.hasMoreTokens()){
        String token = tokenizer.nextToken();
        if(token.contains("=")) {
          String key = token.substring(0, token.indexOf('='));
          String value = token.substring(token.indexOf('=') + 1, token.length());
          props.put(key, value);
        }
      }
      return type.cast(props);
    }
  };

  /** . */
  public static final ValueType<ObjectName> OBJECT_NAME = new ValueType<ObjectName>(ObjectName.class) {
    @Override
    public <S extends ObjectName> S parse(Class<S> type, String s) throws Exception {
      return type.cast(ObjectName.getInstance(s));
    }
  };

  /** . */
  protected final Class<V> type;

  /** . */
  protected final Class<? extends Completer> completer;

  protected ValueType(Class<V> type, Class<? extends Completer> completer) throws NullPointerException {
    if (type == null) {
      throw new NullPointerException("No null value type accepted");
    }
    if (completer == null) {
      throw new NullPointerException("No null completer accepted");
    }

    //
    this.completer = completer;
    this.type = type;
  }

  protected ValueType(Class<V> type) throws NullPointerException {
    if (type == null) {
      throw new NullPointerException("No null value type accepted");
    }

    //
    this.completer = EmptyCompleter.class;
    this.type = type;
  }

  final int getDistance(Class<?> clazz) {
    if (type == clazz) {
      return 0;
    } else if (type.isAssignableFrom(clazz)) {
      int degree = 0;
      for (Class<?> current = clazz;current != type;current = current.getSuperclass()) {
        degree++;
      }
      return degree;
    } else {
      return -1;
    }
  }

  @Override
  public final int hashCode() {
    return type.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null) {
      return false;
    } else {
      if (obj == this) {
        return true;
      } else {
        if (obj.getClass() == ValueType.class) {
          ValueType that = (ValueType)obj;
          return type == that.type;
        } else {
          return false;
        }
      }
    }
  }

  public Class<? extends Completer> getCompleter() {
    return completer;
  }

  public final Class<V> getType() {
    return type;
  }

  public final V parse(String s) throws Exception {
    return parse(type, s);
  }

  public abstract <S extends V> S parse(Class<S> type, String s) throws Exception;

}
