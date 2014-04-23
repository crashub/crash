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

package org.crsh.util;

import java.io.Serializable;

/**
 * An immutable sequence of white spaces.
 */
public class BlankSequence implements CharSequence, Serializable {

  /** . */
  private static final BlankSequence[] CACHE = new BlankSequence[64];

  static {
    for (int i = 0;i < CACHE.length;i++) {
      CACHE[i] = new BlankSequence(i);
    }
  }

  public static BlankSequence create(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("No negative length accepted");
    }
    if (length < CACHE.length) {
      return CACHE[length];
    } else {
      return new BlankSequence(length);
    }
  }

  /** . */
  private final int length;

  /** . */
  private String value;

  /**
   * Build a new blank sequence.
   *
   * @param length the length
   * @throws IllegalArgumentException when length is negative
   */
  private BlankSequence(int length) throws IllegalArgumentException {
    if (length < 0) {
      throw new IllegalArgumentException();
    }

    //
    this.length = length;
    this.value = null;
  }

  public int length() {
    return length;
  }

  public char charAt(int index) {
    checkIndex("index", index);
    return ' ';
  }

  public CharSequence subSequence(int start, int end) {
    checkIndex("start", start);
    checkIndex("end", end);
    if (start > end) {
      throw new IndexOutOfBoundsException("Start " + start + " cannot greater than end " + end);
    }
    return new BlankSequence(end - start);
  }

  @Override
  public String toString() {
    if (value == null) {
      if (length == 0) {
        value = "";
      } else {
        char[] chars = new char[length];
        for (int i = 0;i < length;i++) {
          chars[i] = ' ';
        }
        value = new String(chars, 0, chars.length);
      }
    }
    return value;
  }

  private void checkIndex(String name, int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("No negative " + name + " value " + index);
    }
    if (index > length) {
      throw new IndexOutOfBoundsException("The " + name + " value " + index + " cannot greater than length " + length);
    }
  }
}
