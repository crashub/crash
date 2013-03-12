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

package org.crsh.cli.completers;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class FileCompleter extends AbstractPathCompleter<File> {

  @Override
  protected String getCurrentPath() throws Exception {
    return new File(".").getCanonicalPath();
  }

  @Override
  protected File getPath(String path) {
    return new File(path);
  }

  @Override
  protected boolean exists(File path) {
    return path.exists();
  }

  @Override
  protected boolean isDirectory(File path) {
    return path.isDirectory();
  }

  @Override
  protected boolean isFile(File path) {
    return path.isFile();
  }

  @Override
  protected Collection<File> getChilren(File path) {
    File[] files = path.listFiles();
    return files != null ? Arrays.asList(files) : null;
  }

  @Override
  protected String getName(File path) {
    return path.getName();
  }
}
