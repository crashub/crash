/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

package org.crsh.command.impl;

import org.crsh.command.InvocationContext;

import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AttributesMap extends AbstractMap<String, Object> {

  /** . */
  private InvocationContext<?, ?> context;

  /** . */
  private Map<String, Object> delegate;

  /** . */
  private Set<Entry<String, Object>> entries;

  public AttributesMap(InvocationContext<?, ?> context, Map<String, Object> delegate) {
    this.context = context;
    this.delegate = delegate;
  }

  private Entry<String, Object> out = new Entry<String, Object>() {
    public String getKey() {
      return "out";
    }
    public Object getValue() {
      return context.getWriter();
    }
    public Object setValue(Object value) {
      throw new IllegalArgumentException("Cannot change out entry");
    }
  };

  @Override
  public Object get(Object key) {
    if ("out".equals(key)) {
      return out.getValue();
    } else {
      return delegate.get(key);
    }
  }

  @Override
  public Object put(String key, Object value) {
    if ("out".equals(key)) {
      throw new IllegalArgumentException("Cannot change out entry");
    } else {
      return delegate.put(key, value);
    }
  }

  @Override
  public boolean containsKey(Object key) {
    if ("out".equals(key)) {
      return true;
    } else {
      return delegate.containsKey(key);
    }
  }

  @Override
  public Object remove(Object key) {
    if ("out".equals(key)) {
      throw new IllegalArgumentException("Cannot change out entry");
    } else {
      return delegate.remove(key);
    }
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Cannot clear the attribute map");
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    if (entries == null) {
      entries = new AbstractSet<Entry<String, Object>>() {
        @Override
        public Iterator<Entry<String, Object>> iterator() {
          return new Iterator<Entry<String, Object>>() {
            Iterator<Entry<String, Object>> i = delegate.entrySet().iterator();
            Entry<String, Object> next = out;
            public boolean hasNext() {
              while (next == null && i.hasNext()) {
                Entry<String, Object> tmp = i.next();
                if (!next.getKey().equals("out")) {
                  next = tmp;
                }
              }
              return next != null;
            }
            public Entry<String, Object> next() {
              if (hasNext()) {
                Entry<String, Object> tmp = next;
                next = null;
                return tmp;
              } else {
                throw new NoSuchElementException();
              }
            }
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
        @Override
        public int size() {
          return delegate.size() + (delegate.containsKey("out") ? 0 : 1);
        }
      };
    }
    return entries;
  }
}
