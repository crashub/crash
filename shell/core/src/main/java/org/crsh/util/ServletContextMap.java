package org.crsh.util;

import javax.servlet.ServletContext;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletContextMap extends AbstractMap<String, Object> {

  /** . */
  private final ServletContext context;

  public ServletContextMap(ServletContext context) {
    this.context = context;
  }

  @Override
  public boolean containsKey(Object key) {
    return key instanceof String && context.getAttribute((String)key) != null;
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      return context.getAttribute((String)key);
    } else {
      return null;
    }
  }

  private AbstractSet<Entry<String, Object>> entries = new AbstractSet<Entry<String, Object>>() {

    @Override
    public Iterator<Entry<String, Object>> iterator() {
      final Enumeration<String> names = context.getAttributeNames();
      return new Iterator<Entry<String, Object>>() {
        public boolean hasNext() {
          return names.hasMoreElements();
        }

        public Entry<String, Object> next() {
          final String name = names.nextElement();
          return new Entry<String, Object>() {
            public String getKey() {
              return name;
            }

            public Object getValue() {
              return context.getAttribute(name);
            }

            public Object setValue(Object value) {
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
      for (Enumeration<String> names = context.getAttributeNames();names.hasMoreElements();) {
        size++;
      }
      return size;
    }
  };


  @Override
  public Set<Entry<String, Object>> entrySet() {
    return entries;
  }
}
