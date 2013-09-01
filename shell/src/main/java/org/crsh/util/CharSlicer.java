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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharSlicer {

  /** . */
  private final String value;

  /** . */
  private Pair<Integer, Integer> size;

  public CharSlicer(String value) {
    this.value = value;
    this.size = size();
  }

  public Pair<Integer, Integer> size() {
    if (size == null) {
      size = size(value, 0, 1);
    }
    return size;
  }

  private static Pair<Integer, Integer> size(String s, int index, int height) {
    if (height < 1) {
      throw new IllegalArgumentException("A non positive height=" + height + " cannot be accepted");
    }
    if (index < s.length()) {
      int pos = s.indexOf('\n', index);
      if (pos == -1) {
        return Pair.of(s.length() - index, height);
      } else {
        Pair<Integer, Integer> ret = size(s, pos + 1, height + 1);
        return new Pair<Integer, Integer>(Math.max(pos - index, ret.getFirst()), ret.getSecond());
      }
    } else {
      return Pair.of(0, height);
    }
  }

  public Pair<Integer, Integer>[] lines(final int width) {
    return lines(linesIterator(width), 0);
  }

  private Pair<Integer, Integer>[] lines(Iterator<Pair<Integer, Integer>> i, int count) {
    Pair<Integer, Integer>[] lines;
    if (i.hasNext()) {
      Pair<Integer, Integer> n = i.next();
      lines = lines(i, count + 1);
      lines[count] = n;
    } else {
      lines = new Pair[count];
    }
    return lines;
  }

  public Iterator<Pair<Integer, Integer>> linesIterator(final int width) {
    if (width < 1) {
      throw new IllegalArgumentException("A non positive width=" + width + " cannot be accepted");
    }
    return new BaseIterator<Pair<Integer, Integer>>() {

      /** . */
      int index = 0;

      /** . */
      Pair<Integer, Integer> next = null;

      public boolean hasNext() {
        if (next == null) {
          if (index != Integer.MAX_VALUE) {
            int pos = value.indexOf('\n', index);
            int nextIndex;
            if (pos == -1) {
              pos = Math.min(index + width, value.length());
              nextIndex = pos;
            } else {
              if (pos <= index + width) {
                nextIndex = pos + 1;
              } else {
                nextIndex = pos = index + width;
              }
            }
            next = Pair.of(index, pos);
            if (pos < value.length()) {
              index = nextIndex;
            } else {
              // Stop value
              index = Integer.MAX_VALUE;
            }
          }
        }
        return next != null;
      }

      public Pair<Integer, Integer> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Pair<Integer, Integer> next = this.next;
        this.next = null;
        return next;
      }
    };
  }
}
