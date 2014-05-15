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
import java.util.Iterator;

/**
 * <p>A driver for the file system, this interface is the Service Provider Interface (SPI) part
 * provided by CRaSH.</p>
 *
 * <p>The driver works with handles which are opaque objects that model a path in the file system,
 * an handle can represent a file or a directory and is considered as non modifiable from the file
 * system perspective.</p>
 *
 * @param <H> the handle generic type.
 */
public interface FSDriver<H> {

  /**
   * Returns the root handle.
   *
   * @return the root handle
   * @throws IOException any io exception
   */
  H root() throws IOException;

  /**
   * Returns the name of the handle.
   *
   * @param handle the handle
   * @return the handle name
   * @throws IOException any io exception
   */
  String name(H handle) throws IOException;

  /**
   * Returns true if the handle represent a directory.
   *
   * @param handle the handle
   * @return true if the handle is a directory, false otherwise
   * @throws IOException any io exception
   */
  boolean isDir(H handle) throws IOException;

  /**
   * Return the specific child of a directory handle, null should be returned if no such
   * child exist.
   *
   * @param handle the directory handle
   * @param name the child name
   * @return the specified child
   * @throws IOException any io exception
   */
  H child(H handle, String name) throws IOException;

  /**
   * Returns an iterable over the children of of a specific directory handle.
   *
   * @param handle the directory handle
   * @return the children as an iterable
   * @throws IOException any io exception
   */
  Iterable<H> children(H handle) throws IOException;

  /**
   * Return the last modified date timestamp of an handle.
   *
   * @param handle the handle
   * @return the last modified timestamp
   * @throws IOException any io exception
   */
  long getLastModified(H handle) throws IOException;

  /**
   * Return an iterator over the resources represented by the specified file handle.
   *
   * @param handle the file handle
   * @return the iterator
   * @throws IOException any io exception
   */
  Iterator<InputStream> open(H handle) throws IOException;

}
