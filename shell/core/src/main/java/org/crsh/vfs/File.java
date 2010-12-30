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

package org.crsh.vfs;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class File<H> {

  /** . */
  private final H handle;

  /** . */
  private VFS<H> fs;

  File(VFS<H> fs, H handle) {
    this.fs = fs;
    this.handle = handle;
  }

  public String getName() throws IOException {
    return fs.driver.name(handle);
  }

  public URL toURL() throws IOException {
    return fs.driver.toURL(handle);
  }

  public boolean isDir() throws IOException {
    return fs.driver.isDir(handle);
  }

  public File<H> child(String name) throws IOException {
    for (H childHandle : fs.driver.children(handle)) {
      File<H> child = fs.get(childHandle);
      if (child.getName().equals(name)) {
        return child;
      }
    }
    return null;
  }

  public Iterable<File<H>> children() throws IOException {
    final Iterable<H> iterable = fs.driver.children(handle);
    return new Iterable<File<H>>() {
      public Iterator<File<H>> iterator() {
        final Iterator<H> i = iterable.iterator();
        return new Iterator<File<H>>() {
          public boolean hasNext() {
            return i.hasNext();
          }
          public File<H> next() {
            H next = i.next();
            return fs.get(next);
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
