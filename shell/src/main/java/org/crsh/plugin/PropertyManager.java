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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class PropertyManager {

  /** . */
  private static final Logger log = Logger.getLogger(PropertyManager.class.getName());

  /** . */
  private final Map<String, Property<?>> properties;

  PropertyManager() {
    this.properties = new HashMap<String, Property<?>>();
  }

  /**
   * Returns a property value or null if it cannot be found.
   *
   * @param desc the property descriptor
   * @param <T> the property parameter type
   * @return the property value
   * @throws NullPointerException if the descriptor argument is null
   */
  public <T> T getPropertyValue(PropertyDescriptor<T> desc) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    Property<T> property = getProperty(desc.getName(), desc.getType());
    return property != null ? property.getValue() : null;
  }

  /**
   * Returns a property or null if it cannot be found.
   *
   * @param desc the property descriptor
   * @param <T> the property parameter type
   * @return the property object
   * @throws NullPointerException if the descriptor argument is null
   */
  public <T> Property<T> getProperty(PropertyDescriptor<T> desc) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    return getProperty(desc.getName(), desc.getType());
  }

  /**
   * Returns a property or null if it cannot be found.
   *
   * @param propertyName the name of the property
   * @param type the property type
   * @param <T> the property parameter type
   * @return the property object
   * @throws NullPointerException if any argument is null
   */
  private <T> Property<T> getProperty(String propertyName, Class<T> type) throws NullPointerException {
    if (propertyName == null) {
      throw new NullPointerException("No null property name accepted");
    }
    if (type == null) {
      throw new NullPointerException("No null property type accepted");
    }
    Property<?> property = properties.get(propertyName);
    if (property != null) {
      PropertyDescriptor<?> descriptor = property.getDescriptor();
      if (type.equals(descriptor.getType())) {
        return (Property<T>)property;
      }
    }
    return null;
  }

  /**
   * Set a context property to a new value. If the provided value is null, then the property is removed.
   *
   * @param desc the property descriptor
   * @param value the property value
   * @param <T> the property parameter type
   * @throws NullPointerException if the descriptor argument is null
   */
  <T> void setProperty(PropertyDescriptor<T> desc, T value) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException("No null descriptor allowed");
    }
    if (value == null) {
      log.log(Level.FINE, "Removing property " + desc.name);
      properties.remove(desc.getName());
    } else {
      Property<T> property = new Property<T>(desc, value);
      log.log(Level.FINE, "Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc.getName(), property);
    }
  }

  /**
   * Set a context property to a new value.
   *
   * @param desc the property descriptor
   * @param value the property value
   * @param <T> the property parameter type
   * @throws NullPointerException if the descriptor argument or the value is null
   * @throws IllegalArgumentException if the string value cannot be converted to the property type
   */
  <T> void parseProperty(PropertyDescriptor<T> desc, String value) throws NullPointerException, IllegalArgumentException {
    if (desc == null) {
      throw new NullPointerException("No null descriptor allowed");
    }
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    } else {
      Property<T> property = desc.toProperty(value);
      log.log(Level.FINE, "Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc.getName(), property);
    }
  }
}
