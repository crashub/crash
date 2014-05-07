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

package org.crsh.cli.impl.lang;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  /** . */
  static final Object[] EMPTY_ARGS = new Object[0];

  /** . */
  static final Pattern INDENT_PATTERN = Pattern.compile("(?<=^|\\n)[ \\t\\x0B\\f\\r]*(?=\\S)");

  /** . */
  public static final String MAN_TAB = _tab(7);

  /** . */
  public static final String MAN_TAB_EXTRA = _tab(7 + 4);

  /** . */
  static final String[] tabIndex;

  static {
    String[] tmp = new String[20];
    for (int i = 0;i < tmp.length;i++) {
      tmp[i] = _tab(i);
    }
    tabIndex = tmp;
  }

  static String tab(int size) {
    if (size < 0) {
      throw new IllegalArgumentException();
    }
    if (size < tabIndex.length) {
      return tabIndex[size];
    } else {
      return _tab(size);
    }
  }

  private static String _tab(int size) {
    char[] tmp = new char[size];
    Arrays.fill(tmp, ' ');
    return new String(tmp);
  }

  public static <A extends Appendable> A indent(int tab, CharSequence s, A appendable) throws IOException {
    return indent(tab(tab), s, appendable);
  }

  public static <A extends Appendable> A indent(String tab, CharSequence s, A appendable) throws IOException {
    Matcher matcher = INDENT_PATTERN.matcher(s);
    int prev = 0;
    while (matcher.find()) {
      int start = matcher.start();
      appendable.append(s, prev, start);
      appendable.append(tab);
      prev = matcher.end();
    }
    appendable.append(s, prev, s.length());
    return appendable;
  }

  public static <T> Iterable<T[]> tuples(final Class<T> type, final Iterable<? extends T>... iterables) {
    return new Iterable<T[]>() {
      public Iterator<T[]> iterator() {
        return new Iterator<T[]>() {
          private final Iterator<?>[] iterators = new Iterator<?>[iterables.length];
          private T[] next;
          {
            for (int i = 0;i < iterables.length;i++) {
              iterators[i] = iterables[i].iterator();
            }
          }
          public boolean hasNext() {
            if (next == null) {
              T[] tuple = (T[])Array.newInstance(type, 2);
              for (int i = 0;i < iterators.length;i++) {
                Iterator iterator = iterators[i];
                if (iterator.hasNext()) {
                  tuple[i] = type.cast(iterator.next());
                } else {
                  return false;
                }
              }
              next = tuple;
            }
            return true;
          }
          public T[] next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            T[] tmp = next;
            next = null;
            return tmp;
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <T> Iterable<? extends T> join(final Iterable<? extends T>... iterables) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          int index;
          Iterator<? extends T> current;
          T next;
          public boolean hasNext() {
            if (next == null) {
              while ((current == null || !current.hasNext()) && index < iterables.length) {
                current = iterables[index++].iterator();
              }
              if (current != null && current.hasNext()) {
                next = current.next();
              }
            }
            return next != null;
          }
          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            T tmp = next;
            next = null;
            return tmp;
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  /**
   * Wrap an object with an {@link org.crsh.cli.impl.lang.Instance}.
   *
   * @param object the object to wrap
   * @param <T> the instance generic type
   * @return an {@link org.crsh.cli.impl.lang.Instance} wrapping the specified object
   */
  public static <T> Instance<T> wrap(final T object) {
    return new Instance<T>() {
      @Override
      public <T1> T1 resolve(Class<T1> type) {
        return null;
      }
      @Override
      public T get() {
        return object;
      }
    };
  }
}
