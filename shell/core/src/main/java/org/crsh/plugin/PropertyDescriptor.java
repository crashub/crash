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

package org.crsh.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PropertyDescriptor<T> {

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
  public static final PropertyDescriptor<Integer> VFS_REFRESH_PERIOD = new PropertyDescriptor<Integer>(Integer.class, "vfs.refresh_period", null, "The refresh rate period") {
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

  protected PropertyDescriptor(Class<T> type, String name, T defaultValue, String description) {
    if (name == null) {
      throw new AssertionError();
    }
    this.type = type;
    this.name = name;
    this.defaultValue = defaultValue;
    this.description = description;

    //
    INTERNAL_ALL.put(name, this);
  }

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

  public final Property<T> toProperty(String s) throws NullPointerException, IllegalArgumentException {
    T value = parse(s);
    return new Property<T>(this, value);
  }

  protected abstract T doParse(String s);
}
