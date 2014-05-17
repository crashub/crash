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
package org.crsh.lang.impl.java;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

/** @author Julien Viet */
class LoadingClassLoader extends SecureClassLoader {

  /** . */
  private final Map<String, byte[]> definitions;

  /** . */
  private final HashMap<String, Class<?>> classes;

  LoadingClassLoader(ClassLoader parent, Iterable<JavaClassFileObject> files) {
    super(parent);

    //
    HashMap<String, byte[]> definitions = new HashMap<String,byte[]>();
    for (JavaClassFileObject definition : files) {
      definitions.put(definition.getClassName(), definition.getBytes());
    }

    //
    this.definitions = definitions;
    this.classes = new HashMap<String, Class<?>>();
  }

  LoadingClassLoader(Map<String, byte[]> definitions) {
    this.definitions = definitions;
    this.classes = new HashMap<String, Class<?>>();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> clazz = classes.get(name);
    if (clazz == null) {
      byte[] definition = definitions.get(name);
      if (definition == null) {
        return super.findClass(name);
      } else {
        classes.put(name, clazz = super.defineClass(name, definition, 0, definition.length));
      }
    }
    return clazz;
  }
}
