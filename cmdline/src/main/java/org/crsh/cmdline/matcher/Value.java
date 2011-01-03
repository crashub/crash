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

package org.crsh.cmdline.matcher;

import org.crsh.cmdline.Delimiter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Value {

  /** . */
  private final String rawValue;

  /** . */
  private final String value;

  /** . */
  private final Delimiter delimiter;

  /** . */
  private final boolean determined;

  public Value(String s) {

    //
    String value;
    Delimiter delimiter;
    boolean determined;
    if (s != null) {
      if (s.length() == 1) {
        char c = s.charAt(0);
        switch (c) {
          case '\'':
            value = "";
            delimiter = Delimiter.SIMPLE_QUOTE;
            break;
          case '"':
            value = "";
            delimiter = Delimiter.DOUBLE_QUOTE;
            break;
          default:
            value = s;
            delimiter = Delimiter.WHITE_SPACE;
            break;
        }
        determined = false;
      } else if (s.length() >= 2) {
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);
        if (first == '"') {
          delimiter = Delimiter.DOUBLE_QUOTE;
          if (last == '"') {
            value = s.substring(1, s.length() - 1);
            determined = true;
          } else {
            value = s.substring(1);
            determined = false;
          }
        } else if (first == '\'') {
          delimiter = Delimiter.SIMPLE_QUOTE;
          if (last == '\'') {
            value = s.substring(1, s.length() - 1);
            determined = true;
          } else {
            value = s.substring(1);
            determined = false;
          }
        } else {
          delimiter = Delimiter.WHITE_SPACE;
          value = s;
          determined = false;
        }
      } else {
        delimiter = Delimiter.WHITE_SPACE;
        value = s;
        determined = false;
      }
    } else {
      delimiter = null;
      value = null;
      determined = false;
    }


    this.rawValue = s;
    this.value = value;
    this.delimiter = delimiter;
    this.determined = determined;
  }

  public String getRawValue() {
    return rawValue;
  }

  public String getValue() {
    return value;
  }

  public Delimiter getDelimiter() {
    return delimiter;
  }

  public boolean isDetermined() {
    return determined;
  }

  public boolean isUsable() {
    return value != null && value.length() > 0;
  }

  @Override
  public String toString() {
    return "Value[raw=" + rawValue + "]";
  }
}
