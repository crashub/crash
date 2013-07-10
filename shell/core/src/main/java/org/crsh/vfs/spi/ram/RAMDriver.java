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

package org.crsh.vfs.spi.ram;

import org.crsh.util.Utils;
import org.crsh.vfs.Path;
import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RAMDriver extends AbstractFSDriver<Path> {

  /** . */
  private final Path root;

  /** . */
  final HashMap<Path, String> entries;

  /** . */
  URL baseURL;

  public RAMDriver() {
    try {
      this.root = Path.get("/");
      this.entries = new HashMap<Path, String>();
      this.baseURL = new URL("ram", null, 0, "/", new RAMURLStreamHandler(this));
    }
    catch (MalformedURLException e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public void add(String path, String file) {
    add(Path.get(path), file);
  }

  public void add(Path path, String file) {
    entries.put(path, file);
  }

  public Path root() throws IOException {
    return root;
  }

  public String name(Path handle) throws IOException {
    return handle.getName();
  }

  public boolean isDir(Path handle) throws IOException {
    return handle.isDir();
  }

  public Iterable<Path> children(Path handle) throws IOException {
    List<Path> children = Collections.emptyList();
    for (Path entry : entries.keySet()) {
      if (entry.isChildOf(handle)) {
        if (children.isEmpty()) {
          children = new ArrayList<Path>();
        }
        children.add(entry);
      }
    }
    return children;
  }

  public long getLastModified(Path handle) throws IOException {
    return 0;
  }

  public Iterator<InputStream> open(Path handle) throws IOException {
    return Utils.<InputStream>iterator(new ByteArrayInputStream(entries.get(handle).getBytes("UTF-8")));
  }
}
