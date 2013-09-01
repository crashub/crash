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

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.vfs.Resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassCache<T> extends AbstractClassCache<T> {

  /** . */
  private final Map<String, TimestampedObject<Class<? extends T>>> classes = new ConcurrentHashMap<String, TimestampedObject<Class<? extends T>>>();

  /** . */
  private final PluginContext context;

  /** . */
  private final ResourceKind kind;

  public ClassCache(PluginContext context, ClassFactory<T> classFactory, ResourceKind kind) {
    super(classFactory);

    //
    this.context = context;
    this.kind = kind;
  }

  @Override
  protected TimestampedObject<Class<? extends T>> loadClass(String name) {
    return classes.get(name);
  }

  @Override
  protected void saveClass(String name, TimestampedObject<Class<? extends T>> clazz) {
    classes.put(name, clazz);
  }

  @Override
  protected Resource getResource(String name) {
    return context.loadResource(name, kind);
  }
}
