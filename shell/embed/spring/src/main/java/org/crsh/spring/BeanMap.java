package org.crsh.spring;

import org.crsh.util.SimpleMap;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Arrays;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BeanMap extends SimpleMap<String, Object> {

  /** . */
  private final ListableBeanFactory factory;

  BeanMap(ListableBeanFactory factory) {
    this.factory = factory;
  }

  @Override
  protected Iterator<String> keys() {
    return Arrays.asList(factory.getBeanDefinitionNames()).iterator();
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      return factory.getBean((String)key);
    } else {
      return null;
    }
  }
}
