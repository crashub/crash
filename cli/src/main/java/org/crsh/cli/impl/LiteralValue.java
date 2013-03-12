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

package org.crsh.cli.impl;

public final class LiteralValue {

  /** . */
  private final String rawValue;

  /** . */
  private final String value;

  public LiteralValue(String rawValue, String value) {
    if (rawValue == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }
    if (rawValue.length() == 0) {
      throw new IllegalArgumentException();
    }
    this.rawValue = rawValue;
    this.value = value;
  }

  /**
   * Returns the value as expressed in the command line.
   *
   * @return the raw value
   */
  public String getRawValue() {
    return rawValue;
  }

  /**
   * Returns the value as interpreted by the context.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Value[raw=" + rawValue + "]";
  }
}
