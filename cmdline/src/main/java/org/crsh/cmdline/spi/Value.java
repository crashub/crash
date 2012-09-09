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

package org.crsh.cmdline.spi;

import java.util.StringTokenizer;

public abstract class Value {

  public static class Properties extends Value {

    public Properties(String string) throws NullPointerException {
      super(string);
    }

    public java.util.Properties getProperties() {
      java.util.Properties props = new java.util.Properties();
      StringTokenizer tokenizer = new StringTokenizer(getString(), ";", false);
      while(tokenizer.hasMoreTokens()){
        String token = tokenizer.nextToken();
        if(token.contains("=")) {
          String key = token.substring(0, token.indexOf('='));
          String value = token.substring(token.indexOf('=') + 1, token.length());
          props.put(key, value);
        }
      }
      return props;
    }
  }

  /** . */
  private final String string;

  /**
   * The only constructors that accepts a string.
   *
   * @param string the string value
   * @throws NullPointerException if the string is null
   */
  public Value(String string) throws NullPointerException {
    if (string == null) {
      throw new NullPointerException("No null string accepted");
    }
    this.string = string;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() ^ string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj != null && obj.getClass().equals(getClass())) {
      Value that = (Value)obj;
      return string.equals(that.string);
    }
    return false;
  }

  /**
   * Returns the string value.
   *
   * @return the string value
   */
  public final String getString() {
    return string;
  }

  @Override
  public final String toString() {
    return string;
  }
}
