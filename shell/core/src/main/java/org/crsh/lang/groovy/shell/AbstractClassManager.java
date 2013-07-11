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

package org.crsh.lang.groovy.shell;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.CommandInvoker;
import org.crsh.command.NoSuchCommandException;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.ErrorType;
import org.crsh.util.TimestampedObject;
import org.crsh.vfs.Resource;

import java.io.UnsupportedEncodingException;

public abstract class AbstractClassManager<T> {

  /** . */
  private final PluginContext context;

  /** . */
  private final CompilerConfiguration config;

  /** . */
  private final Class<T> baseClass;

  protected AbstractClassManager(PluginContext context, Class<T> baseClass, Class<? extends Script> baseScriptClass) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(baseScriptClass.getName());

    //
    this.context = context;
    this.config = config;
    this.baseClass = baseClass;
  }
  
  protected abstract TimestampedObject<Class<? extends T>> loadClass(String name);

  protected abstract void saveClass(String name, TimestampedObject<Class<? extends T>> clazz);

  protected abstract Resource getResource(String name);

  Class<? extends T> getClass(String name) throws NoSuchCommandException, NullPointerException {
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
          throw new NoSuchCommandException(name, ErrorType.INTERNAL, "Could not compile command script " + name, e);
        }

        //
        Class<?> clazz;
        try {
          GroovyCodeSource gcs = new GroovyCodeSource(source, name, "/groovy/shell");
          GroovyClassLoader gcl = new GroovyClassLoader(context.getLoader(), config);
          clazz = gcl.parseClass(gcs, false);
        }
        catch (NoClassDefFoundError e) {
          throw new NoSuchCommandException(name, ErrorType.INTERNAL, "Could not compile command script " + name, e);
        }
        catch (CompilationFailedException e) {
          throw new NoSuchCommandException(name, ErrorType.INTERNAL, "Could not compile command script " + name, e);
        }

        //
        if (baseClass.isAssignableFrom(clazz)) {
          Class<? extends T> providerClass = clazz.asSubclass(baseClass);
          providerRef = new TimestampedObject<Class<? extends T>>(script.getTimestamp(), providerClass);
          saveClass(name, providerRef);
        } else {
          throw new NoSuchCommandException(name, ErrorType.INTERNAL, "Parsed script " + clazz.getName() +
            " does not implements " + CommandInvoker.class.getName());
        }
      }
    }

    //
    if (providerRef == null) {
      return null;
    }

    //
    return providerRef.getObject();
  }

  T getInstance(String name) throws NoSuchCommandException, NullPointerException {
    Class<? extends T> clazz = getClass(name);
    if (clazz == null) {
      return null;
    }

    //
    try {
      return clazz.newInstance();
    }
    catch (Exception e) {
      throw new NoSuchCommandException(name, ErrorType.INTERNAL, "Could not create command " + name + " instance", e);
    }
  }
}
