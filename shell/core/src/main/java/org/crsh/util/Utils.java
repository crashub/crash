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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

  /** . */
  private static final Iterator EMPTY_ITERATOR = Collections.emptyList().iterator();

  public static <E> Iterator<E> iterator() {
    @SuppressWarnings("unchecked")
    Iterator<E> iterator = (Iterator<E>)EMPTY_ITERATOR;
    return iterator;
  }

  public static <E> Iterator<E> iterator(final E element) {
    return new BaseIterator<E>() {
      boolean hasNext = true;
      @Override
      public boolean hasNext() {
        return hasNext;
      }
      @Override
      public E next() {
        if (hasNext) {
          hasNext = false;
          return element;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  public static <E> ArrayList<E> newArrayList() {
    return new ArrayList<E>();
  }

  public static <E> LinkedList<E> newLinkedList() {
    return new LinkedList<E>();
  }

  public static <E> HashSet<E> newHashSet() {
    return new HashSet<E>();
  }

  public static <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<K, V>();
  }

  public static <K, V, M extends Map<K, V>> M map(M map, K key, V value) {
    map.put(key, value);
    return map;
  }

  public static <K, V> HashMap<K, V> map(K key, V value) {
    HashMap<K, V> map = new HashMap<K, V>();
    map.put(key, value);
    return map;
  }

  public static <E> List<E> list(E... elements) {
    return Arrays.asList(elements);
  }

  public static <E> List<E> list(Iterable<E> iterable) {
    return list(iterable.iterator());
  }

  public static <E> List<E> list(Iterator<E> iterator) {
    ArrayList<E> list = new ArrayList<E>();
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

  public static int indexOf(CharSequence s, int off, char c) {
    for (int len = s.length();off < len;off++) {
      if (s.charAt(off) == c) {
        return off;
      }
    }
    return -1;
  }

  public static String trimLeft(String s) {
    if (s == null) {
      throw new NullPointerException("No null string accepted");
    }
    int index = 0;
    int len = s.length();
    while (index < len) {
      if (s.charAt(index) == ' ') {
        index++;
      } else {
        break;
      }
    }
    if (index > 0) {
      return s.substring(index);
    } else {
      return s;
    }
  }

  public static <E> E notNull(E e1, E e2) {
    if (e1 != null) {
      return e1;
    } else {
      return e2;
    }
  }

  private static final Pattern FOO = Pattern.compile("" +
      "(\\*)" + // Wildcard *
      "|" +
      "(\\?)" + // Wildcard ?
      "|" +
      "(?:\\[([^)]+)\\])" + // Range
      "|" +
      "(\\\\.)" // Escape
  );

  /**
   * Create a pattern that transforms a glob expression into a regular expression, the following task are supported
   * <ul>
   *   <li>* : Match any number of unknown characters</li>
   *   <li>? : Match one unknown character</li>
   *   <li>[characters] : Match a character as part of a group of characters</li>
   *   <li>\ : Escape character</li>
   * </ul>
   *
   * @param globex the glob expression
   * @return the regular expression
   * @throws NullPointerException when the globex argument is null
   */
  public static String globexToRegex(String globex) throws NullPointerException {
    if (globex == null) {
      throw new NullPointerException("No null globex accepted");
    }
    StringBuilder regex = new StringBuilder();
    int prev = 0;
    Matcher matcher = FOO.matcher(globex);
    while (matcher.find()) {
      int next = matcher.start();
      if (next > prev) {
        regex.append(Pattern.quote(globex.substring(prev, next)));
      }
      if (matcher.group(1) != null) {
        regex.append(".*");
      } else if (matcher.group(2) != null) {
        regex.append(".");
      } else if (matcher.group(3) != null) {
        regex.append("[");
        regex.append(Pattern.quote(matcher.group(3)));
        regex.append("]");
      } else if (matcher.group(4) != null) {
        regex.append(Pattern.quote(Character.toString(matcher.group(4).charAt(1))));
      } else {
        throw new UnsupportedOperationException("Not handled yet");
      }
      prev = matcher.end();
    }
    if (prev < globex.length()) {
      regex.append(Pattern.quote(globex.substring(prev)));
    }
    return regex.toString();
  }

}
