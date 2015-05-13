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

import org.crsh.vfs.Path;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.Mount;

import java.io.File;
import java.io.IOException;

/**
 * @author Julien Viet
 */
public class FileMountFactory implements FSMountFactory<File> {

  /** . */
  private final File root;

  /** . */
  private final File absoluteRoot;

  public FileMountFactory(File root) throws IOException {
    if (root == null) {
      throw new NullPointerException();
    }

    //
    File absoluteRoot = root;
    while (true) {
      File parent = absoluteRoot.getParentFile();
      if (parent != null && parent.isDirectory()) {
        absoluteRoot = parent;
      } else {
        break;
      }
    }

    //
    this.root = root;
    this.absoluteRoot = absoluteRoot;
  }

  @Override
  public Mount<File> create(Path path) throws IOException {
    File file = path.isAbsolute() ? absoluteRoot : root;

    for (int i = 0; i < path.getSize(); i++) {
      String name = path.nameAt(i);
      if(i == 0 && isWindow() && name.length() == 2 && name.charAt(1) == ':') {
        file = new File(name + File.separatorChar);
      } else {
        file = new File(file, name);
      }
    }

    // Always use absolute path here
    return new Mount<File>(new FileDriver(file), "file:" + file.getAbsolutePath());
  }

  private static String OS = System.getProperty("os.name").toLowerCase();
  private static boolean isWindow() {
    return (OS.indexOf("win") >= 0);
  }
}
