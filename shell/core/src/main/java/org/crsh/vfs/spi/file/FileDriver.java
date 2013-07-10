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

package org.crsh.vfs.spi.file;

import org.crsh.util.Utils;
import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class FileDriver extends AbstractFSDriver<File> {

  /** . */
  private final File root;

  /**
   * Create a new file driver.
   *
   * @param root the root
   * @throws NullPointerException if the root argument is null
   */
  public FileDriver(File root) throws NullPointerException {
    if (root == null) {
      throw new NullPointerException();
    }

    //
    this.root = root;
  }

  public File root() throws IOException {
    return root;
  }

  public String name(File handle) throws IOException {
    return handle.getName();
  }

  public boolean isDir(File handle) throws IOException {
    return handle.isDirectory();
  }

  public Iterable<File> children(File handle) throws IOException {
    File[] files = handle.listFiles();
    return files != null ? Arrays.asList(files) : Collections.<File>emptyList();
  }

  public long getLastModified(File handle) throws IOException {
    return handle.lastModified();
  }

  public Iterator<InputStream> open(File handle) throws IOException {
    return Utils.<InputStream>iterator(new FileInputStream(handle));
  }
}
