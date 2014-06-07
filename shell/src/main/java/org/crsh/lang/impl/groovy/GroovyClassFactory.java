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
package org.crsh.lang.impl.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.util.ClassFactory;

/** @author Julien Viet */
class GroovyClassFactory<T> extends ClassFactory<T> {

  /** . */
  private final ClassLoader baseLoader;

  /** . */
  private final Class<T> baseClass;

  /** . */
  final CompilerConfiguration config;

  GroovyClassFactory(ClassLoader baseLoader, Class<T> baseClass, Class<? extends Script> baseScriptClass) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(baseScriptClass.getName());

    //
    this.baseLoader = baseLoader;
    this.baseClass = baseClass;
    this.config = config;
  }

  @Override
  public Class<? extends T> parse(String name, String source) throws CommandException {
    Class<?> clazz;
    try {
      GroovyCodeSource gcs = new GroovyCodeSource(source, name, "/groovy/shell");
      GroovyClassLoader gcl = new GroovyClassLoader(baseLoader, config);
      clazz = gcl.parseClass(gcs, false);
    }
    catch (NoClassDefFoundError e) {
      throw new CommandException(ErrorKind.INTERNAL, "Could not compile command script " + name, e);
    }
    catch (CompilationFailedException e) {
      throw new CommandException(ErrorKind.INTERNAL, "Could not compile command script " + name, e);
    }

    if (baseClass.isAssignableFrom(clazz)) {
      return clazz.asSubclass(baseClass);
    } else {
      throw new CommandException(ErrorKind.INTERNAL, "Parsed script " + clazz.getName() +
          " does not implements " + baseClass.getName());
    }
  }
}
