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

public class Property<T> {

  /** . */
  private final PropertyDescriptor<T> desc;

  /** . */
  private final T value;

  public Property(PropertyDescriptor<T> desc, T value) {
    if (desc == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }
    if (!desc.type.isInstance(value)) {
      throw new IllegalArgumentException("Property value is not of correct type " + desc.type);
    }

    //
    this.desc = desc;
    this.value = value;
  }

  public PropertyDescriptor<T> getDescriptor() {
    return desc;
  }

  public T getValue() {
    return value;
  }

  /**
   * @return the value formatted as a String, if the property descriptor is secret, the effective value will not
   *         be releaved
   */
  public String getDisplayValue() {
    return desc.secret ? "*****" : String.valueOf(value);
  }
}
