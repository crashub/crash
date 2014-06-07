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

import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.vfs.Resource;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassCache<T> {

  /** . */
  private final ClassFactory<T> classFactory;

  /** . */
  private final Map<String, TimestampedObject<Class<? extends T>>> classes = new ConcurrentHashMap<String, TimestampedObject<Class<? extends T>>>();

  /** . */
  private final PluginContext context;

  /** . */
  private final ResourceKind kind;

  public ClassCache(PluginContext context, ClassFactory<T> classFactory, ResourceKind kind) {
    this.classFactory = classFactory;
    this.context = context;
    this.kind = kind;
  }

  private TimestampedObject<Class<? extends T>> loadClass(String name) {
    return classes.get(name);
  }

  private void saveClass(String name, TimestampedObject<Class<? extends T>> clazz) {
    classes.put(name, clazz);
  }

  private Resource getResource(String name) {
    return context.loadResource(name, kind);
  }

  public TimestampedObject<Class<? extends T>> getClass(String name) throws CommandException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null argument allowed");
    }

    TimestampedObject<Class<? extends T>> providerRef = loadClass(name);

    //
    Resource script = getResource(name);

    //
    if (script != null) {
      if (providerRef != null) {
        if (script.getTimestamp() != providerRef.getTimestamp()) {
          providerRef = null;
        }
      }

      //
      if (providerRef == null) {

        //
        String source;
        try {
          source = new String(script.getContent(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          throw new CommandException(ErrorKind.INTERNAL, "Could not compile command script " + name, e);
        }

        //
        Class<? extends T> clazz = classFactory.parse(name, source);
        providerRef = new TimestampedObject<Class<? extends T>>(script.getTimestamp(), clazz);
        saveClass(name, providerRef);
      }
    }

    //
    if (providerRef == null) {
      return null;
    }

    //
    return providerRef;
  }
}
