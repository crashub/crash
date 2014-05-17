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

import org.crsh.util.Utils;
import org.crsh.vfs.spi.url.Node;
import org.crsh.vfs.spi.url.Resource;
import org.crsh.vfs.spi.url.URLDriver;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** @author Julien Viet */
class ClasspathResolver {

  /** . */
  final ClassLoader loader;

  /** . */
  final URLDriver driver;

  public ClasspathResolver(ClassLoader loader) {

    URLDriver driver = null;
    try {
      driver = new URLDriver();
      driver.merge(loader);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //
    this.loader = loader;
    this.driver = driver;
  }

  private void resolve(List<JavaFileObject> files, Node node, String binaryName, boolean recurse) throws IOException, URISyntaxException {
    for (Node child : driver.children(node)) {
      Iterator<Resource> i = child.iterator();
      if (i.hasNext()) {
        if (child.name.endsWith(".class")) {
          Resource r = i.next();
          URI uri = r.url.toURI();
          files.add(new NodeJavaFileObject(
              binaryName + "." + child.name.substring(0, child.name.length() - ".class".length()),
              uri,
              r.streamFactory,
              r.lastModified));
        }
      } else {
        if (recurse) {
          resolve(files, child, binaryName + "." + child.name, recurse);
        }
      }
    }
  }

  public Iterable<JavaFileObject> resolve(String pkg, boolean recurse) throws IOException, URISyntaxException {

    Node current = driver.root();

    String[] elts = Utils.split(pkg, '.');

    for (String elt : elts) {
      current = driver.child(current, elt);
      if (current == null) {
        return Collections.emptyList();
      }
    }

    //
    List<JavaFileObject> files = new ArrayList<JavaFileObject>();
    resolve(files, current, pkg, recurse);
    return files;



/*
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
            ret.add(new URIJavaFileObject(binaryName, entryURI, i.getStreamFactory(), entry.getTime()));
          }
        }
      } else {
        throw new UnsupportedOperationException("Protocol for url " + url + " not supported");
      }
    }
    return ret;
*/
  }

/*
  private void resolve(String pkgName, ArrayList<JavaFileObject> ret, File file, boolean recurse) {
    final File[] children = file.listFiles();
    if (children != null) {
      Arrays.sort(children);
      for (final File child : children) {
        if (child.isDirectory()) {
          if (recurse) {
            resolve(pkgName, ret, child, recurse);
          }
        } else {
          String childName = child.getName();
          if (childName.endsWith(".class")) {
            String binaryName = pkgName + "." + childName.substring(0, childName.length() - ".class".length());
            InputStreamFactory streamFactory = new InputStreamFactory() {
              @Override
              public InputStream open() throws IOException {
                return new FileInputStream(child);
              }
            };
            ret.add(new URIJavaFileObject(binaryName, child.toURI(), streamFactory, child.lastModified()));
          }
        }
      }
    }
  }
*/
}
