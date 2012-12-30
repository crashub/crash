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

package org.crsh.vfs;

import org.crsh.util.BaseIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Path implements Iterable<String> {

  /** . */
  private static final String[] EMPTY_STRING = new String[0];

  /** . */
  private final boolean dir;

  /** . */
  private final String[] names;

  /** . */
  private String value;

  public static Path get(Path parent, String name, boolean dir) {
    if (!parent.dir) {
      throw new IllegalArgumentException("Not a dir");
    }
    int length = parent.names.length;
    String[] names = new String[length + 1];
    System.arraycopy(parent.names, 0, names, 0, length);
    names[length] = name;
    return new Path(dir, names);
  }

  public static Path get(String s) {
    if (s.length() == 0) {
      throw new IllegalArgumentException("No empty path");
    }
    if (s.charAt(0) != '/') {
      throw new IllegalArgumentException("Path " + s + " must begin with a '/'");
    }

    //
    int start = 0;
    int end = s.length();
    String[] names = EMPTY_STRING;
    while (start < end) {
      if (s.charAt(end - 1) == '/') {
        end--;
      } else if (s.charAt(start) == '/') {
        start++;
      } else {
        names = parseNames(s, start, end, 0);
        break;
      }
    }

    //
    return new Path(end < s.length(), names);
  }

  private static String[] parseNames(final String s, final int prev, int end, final int count) {
    int next = s.indexOf('/', prev);
    if (next == -1 || next > end) {
      if (prev < end) {
        String[] ret = new String[count + 1];
        ret[count] = s.substring(prev);
        return ret;
      } else {
        return new String[count];
      }
    } else if (next - prev > 0) {
      String[] ret = parseNames(s, next + 1, end, count + 1);
      ret[count] = s.substring(prev, next);
      return ret;
    } else {
      return parseNames(s, next + 1, end, count);
    }
  }

  private Path(boolean dir, String[] names) {
    this.dir = dir;
    this.names = names;
  }

  public Iterator<String> iterator() {
    return new BaseIterator<String>() {
      int index = 0;
      public boolean hasNext() {
        return index < names.length;
      }
      public String next() {
        if (index < names.length) {
          return names[index++];
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  public int getSize() {
    return names.length;
  }

  public boolean isDir() {
    return dir;
  }

  public String getName() {
    return names.length > 0 ? names[names.length - 1] : "";
  }

  public boolean isChildOf(Path parent) {
    if (parent.dir) {
      int length = parent.names.length;
      if (names.length == length + 1) {
        for (int i = 0;i < length;i++) {
          if (names[i].equals(parent.names[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Path) {
      Path that = (Path)o;
      int length = that.names.length;
      if (names.length == length) {
        for (int i = 0;i < length;i++) {
          if (!names[i].equals(that.names[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = dir ? 1 : 0;
    for (int i = names.length - 1;i >= 0;i--) {
      hashCode = hashCode * 41 + names[i].hashCode();
    }
    return hashCode;
  }

  /**
   * Returns the canonical path value.
   *
   * @return the value
   */
  public String getValue() {
    if (value == null) {
      StringBuilder sb = new StringBuilder(8 * names.length);
      for (String name : names) {
        sb.append('/').append(name);
      }
      if (dir) {
        sb.append('/');
      }
      value = sb.toString();
    }
    return value;
  }

  public String toString() {
    return "Path[value=" + getValue() + "]";
  }
}
