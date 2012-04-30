/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.vfs.spi.jarurl;

import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JarURLDriver extends AbstractFSDriver<Handle> {

  /** . */
  final ClassLoader loader;

  /** . */
  final Handle root;

  /** . */
  final URL jarURL;

  private static Handle get(JarURLDriver driver, Map<String, Handle> handles, String path) {
    Handle handle = handles.get(path);
    if (handle == null) {
      handle = new Handle(driver, path);
      int to = path.length();
      if (path.charAt(to - 1) == '/') {
        to--;
      }
      int from = -1;
      for (int i = to - 1;i >= 0;i--) {
        if (path.charAt(i) == '/') {
          from = i;
          break;
        }
      }
      String name;
      Handle parent;
      if (from == -1) {
        parent = handles.get("");
        name = path.substring(0, to);
      } else {
        parent = get(driver, handles, path.substring(0, from));
        name = path.substring(from + 1, to);
      }
      parent.children.put(name, handle);
      handles.put(path.substring(0, to), handle);
    }
    return handle;
  }

  public JarURLDriver(ClassLoader loader, JarURLConnection conn) throws IOException {
    JarFile file = conn.getJarFile();
    Map<String, Handle> handles = new HashMap<String, Handle>();
    handles.put("", root = new Handle(this, ""));
    for (JarEntry entry : Collections.list(file.entries())) {
      Handle handle = get(this, handles, entry.getName());
      handle.entry = entry;
    }

    //
    this.jarURL = conn.getJarFileURL();
    this.loader = loader;
  }

  public Handle root() throws IOException {
    return root;
  }

  public String name(Handle handle) throws IOException {
    return handle.path.getName();
  }

  public boolean isDir(Handle handle) throws IOException {
    return handle.path.isDir();
  }

  public Iterable<Handle> children(Handle handle) throws IOException {
    return handle.children.values();
  }

  public long getLastModified(Handle handle) throws IOException {
    return 0;
  }

  public InputStream open(Handle handle) throws IOException {
    return loader.getResourceAsStream(handle.entry.getName());
  }
}
