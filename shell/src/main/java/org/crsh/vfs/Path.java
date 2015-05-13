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

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Path implements Iterable<String> {

  /** . */
  private static final String[] EMPTY_STRING = new String[0];

  /** . */
  public static final Absolute ROOT = new Absolute(true, EMPTY_STRING);

  /** . */
  public static final Relative EMPTY = new Relative(true, EMPTY_STRING);

  private static String[] path(java.io.File file, int size) {
    String[] ret;
    java.io.File parent = file.getParentFile();
    if (parent != null && parent.isDirectory()) {
      ret = path(parent, 1 + size);
    } else {
      ret = new String[1 + size];
    }
    ret[ret.length - size - 1] = file.getName();
    return ret;
  }

  public static Path get(java.io.File file) {
    String[] names = path(file, 0);
    if (file.isAbsolute()) {
      return new Absolute(file.isDirectory(), names);
    } else {
      return new Relative(file.isDirectory(), names);
    }
  }

  public static Path get(String s) {
    if (s.length() == 0) {
      return EMPTY;
    }

    //
    int start;
    boolean absolute;
    if (s.charAt(0) != '/') {
      start = 0;
      absolute = false;
    } else {
      if (s.length() == 1) {
        return ROOT;
      } else {
        start = 1;
        absolute = true;
      }
    }

    //
    boolean dir;
    int end;
    if (s.charAt(s.length() - 1) == '/' || s.charAt(s.length() - 1) == File.separatorChar) {
      dir = true;
      end = s.length() - 1;
    } else {
      dir = false;
      end = s.length();
    }

    //
    String[] names = parseNames(s, start, end, 0);

    //
    return absolute ? new Absolute(dir, names) : new Relative(dir, names);
  }

  private static String[] parseNames(final String s, final int prev, int end, final int count) {
    int next = s.indexOf('/', prev);
    if (next == -1) next = s.indexOf(File.separatorChar, prev);

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

  /** . */
  protected final boolean dir;

  /** . */
  protected final String[] names;

  /** . */
  private String value;

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

  public Path append(String name, boolean dir) {
    int length = names.length;
    String[] names = new String[length + 1];
    System.arraycopy(names, 0, names, 0, length);
    names[length] = name;
    return create(dir, names);
  }

  protected abstract Path create(boolean dir, String[] names);

  public abstract boolean isAbsolute();

  public abstract Absolute absolute();

  public int getSize() {
    return names.length;
  }

  public boolean isDir() {
    return dir;
  }

  public String getName() {
    return names.length > 0 ? names[names.length - 1] : "";
  }

  public String nameAt(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= names.length) {
      throw new IndexOutOfBoundsException("Index out of bounds [0" + (names.length - 1) + "]" + index);
    } else {
      return names[index];
    }
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
      if (names.length == 0) {
        if (isAbsolute()) {
          return "/";
        } else {
          return "";
        }
      } else {
        StringBuilder sb = new StringBuilder(8 * names.length);
        if (isAbsolute()) {
          sb.append('/');
        }
        for (int i = 0;i < names.length;i++) {
          if (i > 0) {
            sb.append('/');
          }
          sb.append(names[i]);
        }
        if (dir) {
          sb.append('/');
        }
        value = sb.toString();
      }
    }
    return value;
  }

  public String toString() {
    return "Path[value=" + getValue() + "]";
  }

  public static class Absolute extends Path {

    private Absolute(boolean dir, String[] names) {
      super(dir, names);
    }

    @Override
    public Absolute append(String name, boolean dir) {
      return (Absolute)super.append(name, dir);
    }

    @Override
    protected Absolute create(boolean dir, String[] names) {
      return new Absolute(dir, names);
    }

    @Override
    public Absolute absolute() {
      return this;
    }

    @Override
    public boolean isAbsolute() {
      return true;
    }
  }

  public static class Relative extends Path {

    private Relative(boolean dir, String[] names) {
      super(dir, names);
    }

    @Override
    public Relative append(String name, boolean dir) {
      return (Relative)super.append(name, dir);
    }

    @Override
    protected Relative create(boolean dir, String[] names) {
      return new Relative(dir, names);
    }

    @Override
    public Absolute absolute() {
      return new Absolute(dir, names);
    }

    @Override
    public boolean isAbsolute() {
      return false;
    }
  }
}
