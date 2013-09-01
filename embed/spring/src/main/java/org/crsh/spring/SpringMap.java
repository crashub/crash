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

package org.crsh.spring;

import org.crsh.util.SimpleMap;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

class SpringMap extends SimpleMap<String, Object> {

  /** . */
  private final ListableBeanFactory factory;

  SpringMap(ListableBeanFactory factory) {
    this.factory = factory;
  }

  @Override
  protected Iterator<String> keys() {
    return Arrays.asList(factory.getBeanDefinitionNames()).iterator();
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      return factory.getBean(((String)key));
    }
    return null;
  }
}
