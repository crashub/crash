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

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

/** @author Julien Viet */
class JavaFileManagerImpl extends ForwardingJavaFileManager<StandardJavaFileManager> {

  /** . */
  private final LinkedHashMap<String, JavaClassFileObject> classes = new LinkedHashMap<String, JavaClassFileObject>();

  /** . */
  private final ClasspathResolver finder;

  JavaFileManagerImpl(StandardJavaFileManager fileManager, ClasspathResolver finder) {
    super(fileManager);

    //
    this.finder = finder;
  }

  Collection<JavaClassFileObject> getClasses() {
    return classes.values();
  }

  @Override
  public boolean hasLocation(Location location) {
    return location == StandardLocation.CLASS_PATH || location == StandardLocation.PLATFORM_CLASS_PATH;
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof NodeJavaFileObject) {
      return ((NodeJavaFileObject)file).binaryName;
    }
    else {
      return fileManager.inferBinaryName(location, file);
    }
  }

  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
    if (location == StandardLocation.PLATFORM_CLASS_PATH) {
      return fileManager.list(location, packageName, kinds, recurse);
    }
    else if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
      if (packageName.startsWith("java")) {
        return fileManager.list(location, packageName, kinds, recurse);
      }
      else {
        try {
          Iterable<JavaFileObject> ret = finder.resolve(packageName, recurse);
          return ret;
        }
        catch (URISyntaxException e) {
          throw new IOException(e);
        }
      }
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

    if (location != StandardLocation.CLASS_OUTPUT) {
      throw new IOException("Location " + location + " not supported");
    }
    if (kind != JavaFileObject.Kind.CLASS) {
      throw new IOException("Kind " + kind + " not supported");
    }

    //
    JavaClassFileObject clazz = classes.get(className);
    if (clazz == null) {
      try {
        classes.put(className, clazz = new JavaClassFileObject(className));
      }
      catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
    return clazz;
  }
}
