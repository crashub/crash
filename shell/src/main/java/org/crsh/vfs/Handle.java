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

import org.crsh.util.Utils;
import org.crsh.vfs.spi.FSDriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class Handle<H> {

  /** . */
  private final FSDriver<H> driver;

  /** . */
  final String name;

  /** . */
  final H handle;

  Handle(FSDriver<H> driver, H handle) throws IOException {
    String name = driver.name(handle);

    //
    this.driver = driver;
    this.handle = handle;
    this.name = name;
  }

  Iterable<Handle<H>> children() throws IOException {
    List<Handle<H>> children = new ArrayList<Handle<H>>();
    for (H h : driver.children(handle)) {
      children.add(new Handle<H>(driver, h));
    }
    return children;
  }

  Resource getResource() throws IOException {
    InputStream in = open();
    byte[] bytes = Utils.readAsBytes(in);
    long lastModified = getLastModified();
    return new Resource(name, bytes, lastModified);
  }

  Iterator<Resource> getResources() throws IOException {
    Iterator<InputStream> i = driver.open(handle);
    if (i.hasNext()) {
      LinkedList<Resource> resources = new LinkedList<Resource>();
      while (i.hasNext()) {
        InputStream in = i.next();
        byte[] bytes = Utils.readAsBytes(in);
        long lastModified = getLastModified();
        resources.add(new Resource(name, bytes, lastModified));
      }
      return resources.iterator();
    } else {
      return Utils.iterator();
    }
  }

  private InputStream open() throws IOException {
    Iterator<InputStream> i = driver.open(handle);
    if (i.hasNext()) {
      return i.next();
    } else {
      throw new IOException("No stream");
    }
  }

  long getLastModified() throws IOException {
    return driver.getLastModified(handle);
  }
}
