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

package org.crsh.plugin;

import org.crsh.util.Utils;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class PropertyDescriptor<T> {

  /** The display value returned when a property is secret. */
  public static final String SECRET_DISPLAY_VALUE = "*****";

  public static PropertyDescriptor<String> create(String name, String defaultValue, String description, boolean secret) {
    return new PropertyDescriptor<String>(String.class, name, defaultValue, description, secret) {
      @Override
      protected String doParse(String s) throws Exception {
        return s;
      }
    };
  }


  public static PropertyDescriptor<String> create(String name, String defaultValue, String description) {
    return create(name, defaultValue, description, false);
  }

  public static PropertyDescriptor<Integer> create(String name, Integer defaultValue, String description, boolean secret) {
    return new PropertyDescriptor<Integer>(Integer.class, name, defaultValue, description, secret) {
      @Override
      protected Integer doParse(String s) throws Exception {
        return Integer.parseInt(s);
      }
    };
  }

  public static PropertyDescriptor<Integer> create(String name, Integer defaultValue, String description) {
    return create(name, defaultValue, description, false);
  }

  public static PropertyDescriptor<List> create(String name, List defaultValue, String description, boolean secret) {
    return new PropertyDescriptor<List>(List.class, name, defaultValue, description, secret) {
      @Override
      protected List doParse(String s) throws Exception {
        String[] split = Utils.split(s, ',');
        List<String> list = Arrays.asList(split);
        for (int i = 0;i < list.size();i++) {
          list.set(i, list.get(i).trim());
        }
        return list;
      }
    };
  }

  public static PropertyDescriptor<List> create(String name, List defaultValue, String description) {
    return create(name, defaultValue, description, false);
  }

  /** . */
  private static final Map<String, PropertyDescriptor<?>> INTERNAL_ALL = new HashMap<String, PropertyDescriptor<?>>();

  /** . */
  public static final Map<String, PropertyDescriptor<?>> ALL = Collections.unmodifiableMap(INTERNAL_ALL);

  /** . */
  public static final PropertyDescriptor<TimeUnit> VFS_REFRESH_UNIT = new PropertyDescriptor<TimeUnit>(TimeUnit.class, "vfs.refresh_unit", TimeUnit.SECONDS, "The refresh time unit") {
    @Override
    public TimeUnit doParse(String s) {
      return TimeUnit.valueOf(s);
    }
  };

  /** . */
  public static final PropertyDescriptor<Integer> VFS_REFRESH_PERIOD = PropertyDescriptor.create("vfs.refresh_period", (Integer)null, "The refresh rate period");

  /** . */
  public final Class<T> type;

  /** . */
  public final String name;

  /** . */
  public final T defaultValue;

  /** . */
  public final String description;

  /** . */
  public final boolean secret;

  /**
   * Create a new property descriptor.
   *
   * @param type         the property type
   * @param name         the property name
   * @param defaultValue the default value
   * @param description  the description
   * @throws NullPointerException if the type, name or description is null
   */
  protected PropertyDescriptor(Class<T> type, String name, T defaultValue, String description) throws NullPointerException {
    this(type, name, defaultValue, description, false);
  }

  /**
   * Create a new property descriptor.
   *
   * @param type         the property type
   * @param name         the property name
   * @param defaultValue the default value
   * @param description  the description
   * @param secret       the value is secret (like a password)
   * @throws NullPointerException if the type, name or description is null
   */
  protected PropertyDescriptor(Class<T> type, String name, T defaultValue, String description, boolean secret) throws NullPointerException {
    if (type == null) {
      throw new NullPointerException("No null type accepted");
    }
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    if (description == null) {
      throw new NullPointerException("No null description accepted");
    }

    this.type = type;
    this.name = name;
    this.defaultValue = defaultValue;
    this.description = description;
    this.secret = secret;

    //
    INTERNAL_ALL.put(name, this);
  }

  public final String getName() {
    return name;
  }

  public final String getDescription() {
    return description;
  }

  public final Class<T> getType() {
    return type;
  }

  public final T getDefaultValue() {
    return defaultValue;
  }

  public final String getDefaultDisplayValue() {
    return secret ? SECRET_DISPLAY_VALUE : String.valueOf(defaultValue);
  }

  /**
   * Parse a string representation of a value and returns the corresponding typed value.
   *
   * @param s the string to parse
   * @return the corresponding value
   * @throws NullPointerException     if the argument is null
   * @throws IllegalArgumentException if the string value cannot be parsed for some reason
   */
  public final T parse(String s) throws NullPointerException, IllegalArgumentException {
    if (s == null) {
      throw new NullPointerException("Cannot parse null property values");
    }
    try {
      return doParse(s);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Illegal property value " + s, e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof PropertyDescriptor<?>) {
      PropertyDescriptor<?> that = (PropertyDescriptor<?>)obj;
      return name.equals(that.name) && type.equals(that.type);
    } else {
      return false;
    }
  }

  /**
   * Parse a string representation of a value and returns the correspondig property value.
   *
   * @param s the string to parse
   * @return the corresponding property
   * @throws NullPointerException     if the argument is null
   * @throws IllegalArgumentException if the string value cannot be parsed for some reason
   */
  public final Property<T> toProperty(String s) throws NullPointerException, IllegalArgumentException {
    T value = parse(s);
    return new Property<T>(this, value);
  }

  /**
   * Implements the real parsing, the string argument must nto be null. The returned value must not be null instead an
   * exception must be thrown.
   *
   * @param s the string to parse
   * @return the related value
   * @throws Exception any exception that would prevent parsing to hapen
   */
  protected abstract T doParse(String s) throws Exception;

  @Override
  public final String toString() {
    return "PropertyDescriptor[name=" + name + ",type=" + type.getName() + ",description=" + description + "]";
  }
}
