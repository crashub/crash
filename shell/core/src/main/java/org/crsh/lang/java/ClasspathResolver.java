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
package org.crsh.lang.java;

import org.crsh.util.ZipIterator;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

/** @author Julien Viet */
class ClasspathResolver {

  /** . */
  final ClassLoader loader;

  public ClasspathResolver(ClassLoader loader) {
    this.loader = loader;
  }

  public Iterable<JavaFileObject> resolve(String pkg, boolean recurse) throws IOException, URISyntaxException {
    String pkgName = pkg.replace('.', '/');
    ArrayList<JavaFileObject> ret = new ArrayList<JavaFileObject>();
    final Enumeration<URL> en = loader.getResources(pkgName);
    while (en.hasMoreElements()) {
      URL url = en.nextElement();
      String protocol = url.getProtocol();
      if (protocol.equals("file")) {
        File root = new File(url.toURI());
        resolve(pkgName, ret, root, recurse);
      } else if ("jar".equals(protocol)) {
        String path = url.getPath();
        int index = path.lastIndexOf('!');
        String containerURLs = path.substring(0, index);
        URL containerURL = new URL(containerURLs);
        ZipIterator i = ZipIterator.create(containerURL);
        while (i.hasNext()) {
          ZipEntry entry = i.next();
          String name = entry.getName();
          if (!entry.isDirectory() && name.startsWith(pkgName) && (name.indexOf('/', pkgName.length() + 1) == -1 || recurse)) {
            String binaryName = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            URI entryURI = new URI("jar:" + containerURLs + "!/" + name);
            ret.add(new URIJavaFileObject(binaryName, entryURI, entry.getTime()));
          }
        }
      } else {
        throw new UnsupportedOperationException("Protocol for url " + url + " not supported");
      }
    }
    return ret;
  }

  private void resolve(String pkgName, ArrayList<JavaFileObject> ret, File file, boolean recurse) {
    final File[] children = file.listFiles();
    if (children != null) {
      Arrays.sort(children);
      for (File child : children) {
        if (child.isDirectory()) {
          if (recurse) {
            resolve(pkgName, ret, child, recurse);
          }
        } else {
          String childName = child.getName();
          if (childName.endsWith(".class")) {
            String binaryName = pkgName + "." + childName.substring(0, childName.length() - ".class".length());
            ret.add(new URIJavaFileObject(binaryName, child.toURI(), child.lastModified()));
          }
        }
      }
    }
  }
}
