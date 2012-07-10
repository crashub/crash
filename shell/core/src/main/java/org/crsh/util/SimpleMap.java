package org.crsh.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
      }
      return size;
    }
  };
}
