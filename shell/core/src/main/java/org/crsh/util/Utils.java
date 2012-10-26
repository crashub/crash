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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Utils {

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
}
