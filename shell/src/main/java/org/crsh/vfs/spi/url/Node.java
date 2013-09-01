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

package org.crsh.vfs.spi.url;

import org.crsh.util.InputStreamFactory;
import org.crsh.util.Safe;
import org.crsh.util.ZipIterator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipEntry;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Node {

  /** . */
  final String name;

  /** . */
  HashMap<String, Node> children = new HashMap<String, Node>();

  /** . */
  LinkedList<File> files = new LinkedList<File>();

  public Node() {
    this.name = "";
  }

  private Node(String name) {
    this.name = name;
  }

  void merge(ClassLoader loader) throws IOException, URISyntaxException {

    // Get the root class path files
    for (Enumeration<URL> i = loader.getResources("");i.hasMoreElements();) {
      URL url = i.nextElement();
      merge(url);
    }
    ArrayList<URL> items = Collections.list(loader.getResources("META-INF/MANIFEST.MF"));
    for (URL item : items) {
      if ("jar".equals(item.getProtocol())) {
        String path = item.getPath();
        int pos = path.indexOf("!/");
        URL url = new URL(path.substring(0, pos));
        merge(url);
      } else {
        //
      }
    }
  }

  void merge(URL url) throws IOException, URISyntaxException {
    if (url.getProtocol().equals("file")) {
      try {
        java.io.File f = new java.io.File(url.toURI());
        if (f.isDirectory()) {
          merge(f);
        }
        else if (f.getName().endsWith(".jar")) {
          merge(new URL("jar:" + url + "!/"));
        } else {
          // WTF ?
        }
      }
      catch (URISyntaxException e) {
        throw new IOException(e);
      }
    } else if (url.getProtocol().equals("jar")) {
      int pos = url.getPath().lastIndexOf("!/");
      URL jarURL = new URL(url.getPath().substring(0, pos));
      String path = url.getPath().substring(pos + 2);
      ZipIterator i = ZipIterator.create(jarURL);
      try {
        while (i.hasNext()) {
          ZipEntry entry = i.next();
          if (entry.getName().startsWith(path)) {
            add(url, entry.getName().substring(path.length()), i.open());
          }
        }
      }
      finally {
        Safe.close(i);
      }
    } else {
      if (url.getPath().endsWith(".jar")) {
        merge(new URL("jar:" + url + "!/"));
      } else {
        // WTF ?
      }
    }
  }

  private void merge(java.io.File f) throws IOException {
    java.io.File[] files = f.listFiles();
    if (files != null) {
      for (final java.io.File file : files) {
        String name = file.getName();
        Node child = children.get(name);
        if (file.isDirectory()) {
          if (child == null) {
            Node dir = new Node(name);
            dir.merge(file);
            children.put(name, dir);
          } else {
            child.merge(file);
          }
        }
        else {
          if (child == null) {
            children.put(name, child = new Node(name));
          }
          child.files.add(new File(new InputStreamFactory() {
            public InputStream open() throws IOException {
              return new FileInputStream(file);
            }
          }, file.lastModified()));
        }
      }
    }
  }

  private void add(URL baseURL, String entryName, InputStreamFactory resolver) throws IOException {
    if (entryName.length() > 0 && entryName.charAt(entryName.length() - 1) != '/') {
      add(baseURL, 0, entryName, 1, resolver);
    }
  }

  private void add(URL baseURL, int index, String entryName, long lastModified, InputStreamFactory resolver) throws IOException {
    int next = entryName.indexOf('/', index);
    if (next == -1) {
      String name = entryName.substring(index);
      Node child = children.get(name);
      if (child == null) {
        children.put(name, child = new Node(name));
      }
      child.files.add(new File(resolver, lastModified));
    } else {
      String name = entryName.substring(index, next);
      Node child = children.get(name);
      if (child == null) {
        children.put(name, child = new Node(name));
      }
      child.add(baseURL, next + 1, entryName, lastModified, resolver);
    }
  }

  static class File {

    /** . */
    final InputStreamFactory resolver;

    /** . */
    final long lastModified;

    File(InputStreamFactory url, long lastModified) {
      this.resolver = url;
      this.lastModified = lastModified;
    }
  }
}
