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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public abstract class SimpleMap<K, V> extends AbstractMap<K, V> {

  protected abstract Iterator<K> keys();

  @Override
  public abstract V get(Object key);

  @Override
  public final boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public final Set<Entry<K, V>> entrySet() {
    return entries;
  }

  private AbstractSet<Entry<K, V>> entries = new AbstractSet<Entry<K, V>>() {

    @Override
    public Iterator<Entry<K, V>> iterator() {
      final Iterator<K> names = keys();
      return new Iterator<Entry<K, V>>() {
        public boolean hasNext() {
          return names.hasNext();
        }

        public Entry<K, V> next() {
          final K name = names.next();
          return new Entry<K, V>() {
            public K getKey() {
              return name;
            }

            public V getValue() {
              return get(name);
            }

            public V setValue(V value) {
              throw new UnsupportedOperationException();
            }
          };
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public int size() {
      int size = 0;
      for (Iterator<K> names = keys();names.hasNext();) {
        size++;
        names.next();
      }
      return size;
    }
  };
}
