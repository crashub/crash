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

package org.crsh.vfs.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Path implements Iterable<String> {

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
      throw new IllegalArgumentException("Path must begin with a '/'");
    }

    // Count
    int end = s.length();

    //
    int count = 0;
    int prev = 1;
    while (true) {
      int next = s.indexOf('/', prev);
      if (next == -1) {
        if (prev < end) {
          count++;
        }
        break;
      } else if (next - prev > 0) {
        count++;
      }
      prev = next + 1;
    }

    //
    String[] names = new String[count];
    prev = 1;
    count = 0;
    boolean dir;
    while (true) {
      int next = s.indexOf('/', prev);
      if (next == -1) {
        if (prev < end) {
          names[count] = s.substring(prev);
          dir = false;
        } else {
          dir = true;
        }
        break;
      } else if (next - prev > 0) {
        names[count++] = s.substring(prev, next);
      }
      prev = next + 1;
    }

    //
    return new Path(dir, names);
  }

  private Path(boolean dir, String[] names) {
    this.dir = dir;
    this.names = names;
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {
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
      public void remove() {
        throw new UnsupportedOperationException();
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

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Path) {
      Path that = (Path)o;
      return dir == that.dir && super.equals(that);
    }
    return false;
  }

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

  @Override
  public int hashCode() {
    return super.hashCode() ^ (dir ? 1 : 0);
  }
}
