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
  final Handle root;

  /** . */
  final URL jarURL;

  private static Handle get(JarURLDriver driver, Map<String, Handle> handles, String path) {
    Handle handle = handles.get(path);
    if (handle == null) {
      handle = new Handle(driver, path);
      Handle parent;
      String name;
      int pos = path.lastIndexOf('/');
      if (pos == -1) {
        parent = handles.get("/");
        name = path;
      } else {
        parent = get(driver, handles, path.substring(0, pos + 1));
        name = path.substring(pos + 1);
      }
      parent.children.put(name, handle);
      handles.put(path, handle);
    }
    return handle;
  }

  public JarURLDriver(JarURLConnection conn) throws IOException {
    JarFile file = conn.getJarFile();
    Map<String, Handle> handles = new HashMap<String, Handle>();
    handles.put("/", root = new Handle(this, "/"));
    for (JarEntry entry : Collections.list(file.entries())) {
      Handle handle = get(this, handles, entry.getName());
      handle.entry = entry;
    }

    //
    this.jarURL = conn.getURL();
  }

  public Handle root() throws IOException {
    return root;
  }

  public String name(Handle handle) throws IOException {
    return handle.name;
  }

  public boolean isDir(Handle handle) throws IOException {
    return handle.isDir();
  }

  public Iterable<Handle> children(Handle handle) throws IOException {
    return handle.children.values();
  }

  public URL toURL(Handle handle) throws IOException {
    return handle.toURL();
  }
}
