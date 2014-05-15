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

package org.crsh.vfs;

import org.crsh.vfs.spi.FSDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public final class File {

  /** . */
  private final FS fs;

  /** . */
  private final Path path;

  /** . */
  private LinkedList<Handle<?>> handles;

  /** . */
  private LinkedHashMap<String, File> children;

  public File(FS fs, Path path) {
    this.fs = fs;
    this.path = path;
    this.handles = null;
  }

  public Path getPath() {
    return path;
  }

  public String getName() {
    return path.getName();
  }

  public boolean hasChildren() throws IOException {
    return children().iterator().hasNext();
  }

  public Resource getResource() throws IOException {
    for (Handle handle : getHandles()) {
      Resource resource = handle.getResource();
      if (resource != null) {
        return resource;
      }
    }
    return null;
  }

  public Iterable<Resource> getResources() throws IOException {
    List<Resource> urls = Collections.emptyList();
    for (Handle<?> handle : getHandles()) {
      if (urls.isEmpty()) {
        urls = new ArrayList<Resource>();
      }
      Iterator<Resource> resources = handle.getResources();
      while (resources.hasNext()) {
        Resource resource = resources.next();
        urls.add(resource);
      }
    }
    return urls;
  }

  public File child(String name) throws IOException {
    if (children == null) {
      children();
    }
    return children.get(name);
  }

  public Iterable<File> children() throws IOException {
    if (children == null) {
      LinkedHashMap<String, File> children = new LinkedHashMap<String, File>();
      for (Handle<?> handle : getHandles()) {
        for (Handle<?> childHandle : handle.children()) {
          File child = children.get(childHandle.name);
          if (child == null) {
            child = new File(fs, path.append(childHandle.name, false));
            children.put(childHandle.name, child);
          }
          if (child.handles == null) {
            child.handles = new LinkedList<Handle<?>>();
          }
          child.handles.add(childHandle);
        }
      }
      this.children = children;
    }
    return children.values();
  }

  LinkedList<Handle<?>> getHandles() {
    if (handles == null) {
      LinkedList<Handle<?>> handles = new LinkedList<Handle<?>>();
      for (FSDriver<?> driver : fs.drivers) {
        Handle<?> handle = null;
        try {
          handle = getHandle(driver, path);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        if (handle != null) {
          handles.add(handle);
        }
      }
      this.handles = handles;
    }
    return handles;
  }

  <H> Handle<H> getHandle(FSDriver<H> driver, Path path) throws IOException {
    H current = resolve(driver, driver.root(), path);
    if (current != null) {
      return new Handle<H>(driver, current);
    } else {
      return null;
    }
  }

  private <H> H resolve(FSDriver<H> driver, H current, Path path) throws IOException {
    int index = 0;
    while (current != null && index < path.getSize()) {
      String name = path.nameAt(index++);
      current = driver.child(current, name);
    }
    return current;
  }

  @Override
  public String toString() {
    return "File[path=" + path.getValue() + "]";
  }
}
