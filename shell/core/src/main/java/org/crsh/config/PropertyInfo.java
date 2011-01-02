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

package org.crsh.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PropertyInfo<T> {

  /** . */
  private static final List<PropertyInfo<?>> INTERNAL_ALL = new ArrayList<PropertyInfo<?>>();

  /** . */
  public static final List<PropertyInfo<?>> ALL = Collections.unmodifiableList(INTERNAL_ALL);

  /** . */
  public static final PropertyInfo<Integer> SSH_PORT = new PropertyInfo<Integer>(Integer.class, "ssh.port", 2000, "The SSH port") {
    @Override
    public Integer doParse(String s) {
      return Integer.parseInt(s);
    }
  };

  /** . */
  public static final PropertyInfo<String> SSH_KEYPATH = new PropertyInfo<String>(String.class, "ssh.keypath", null, "The path to the key file") {
    @Override
    public String doParse(String s) {
      return s;
    }
  };

  /** . */
  public static final PropertyInfo<Integer> TELNET_PORT = new PropertyInfo<Integer>(Integer.class, "telnet.port", 5000, "The telnet port") {
    @Override
    public Integer doParse(String s) {
      return Integer.parseInt(s);
    }
  };

  /** . */
  public final Class<T> type;

  /** . */
  public final String name;

  /** . */
  public final T defaultValue;

  /** . */
  public final String description;

  private PropertyInfo(Class<T> type, String name, T defaultValue) {
    this(type, name, defaultValue, null);
  }

  private PropertyInfo(Class<T> type, String name, T defaultValue, String description) {
    if (name == null) {
      throw new AssertionError();
    }
    this.type = type;
    this.name = name;
    this.defaultValue = defaultValue;
    this.description = description;

    //
    INTERNAL_ALL.add(this);
  }

  public final T parse(String s) {
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

  public final ConfigProperty<T> toProperty(String s) {
    T value = parse(s);
    return new ConfigProperty<T>(this, value);
  }

  protected abstract T doParse(String s);
}
