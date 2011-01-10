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

package org.crsh.cmdline;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Util {

  /** . */
  static final String FORMAT_STRING = "   %1$-16s %2$s\n";

  /** . */
  static final int TAB_SIZE = 7;

  /** . */
  static final String TAB;

  static {
    char[] tmp = new char[TAB_SIZE];
    Arrays.fill(tmp, ' ');
    TAB = new String(tmp);
  }

  static <T> Iterable<T[]> tuples(final Class<T> type, final Iterable<? extends T>... iterables) {
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
}
