package org.crsh.util;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletContextMap extends SimpleMap<String, Object> {

  /** . */
  private final ServletContext context;

  public ServletContextMap(ServletContext context) {
    this.context = context;
  }

  @Override
  protected Iterator<String> keys() {
    return new Iterator<String>() {
      Enumeration<String> e = context.getAttributeNames();
      public boolean hasNext() {
        return e.hasMoreElements();
      }
      public String next() {
        return e.nextElement();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      return context.getAttribute((String)key);
    } else {
      return null;
    }
  }
}
