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

package org.crsh.vfs.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

public interface FSDriver<H> {

  /**
   * Returns the root handle.
   *
   * @return the root handle
   * @throws IOException any io exception
   */
  H root() throws IOException;

  String name(H handle) throws IOException;

  boolean isDir(H handle) throws IOException;

  H child(H handle, String name) throws IOException;

  Iterable<H> children(H handle) throws IOException;

  long getLastModified(H handle) throws IOException;

  Iterator<InputStream> open(H handle) throws IOException;

}
