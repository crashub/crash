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

package org.crsh.vfs.spi.url;

import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLDriver extends AbstractFSDriver<Node> {

  /** . */
  private final Node.Dir root;

  public URLDriver() {
    this.root = new Node.Dir();
  }

  public void merge(ClassLoader loader) throws IOException, URISyntaxException {
    root.merge(loader);
  }

  public void merge(URL url) throws IOException, URISyntaxException {
    root.merge(url);
  }

  public Node root() throws IOException {
    return root;
  }

  public String name(Node handle) throws IOException {
    return handle.name;
  }

  public boolean isDir(Node handle) throws IOException {
    return handle instanceof Node.Dir;
  }

  public Iterable<Node> children(Node handle) throws IOException {
    return ((Node.Dir)handle).children.values();
  }

  public long getLastModified(Node handle) throws IOException {
    return handle instanceof Node.File ? ((Node.File)handle).lastModified : 0;
  }

  public InputStream open(Node handle) throws IOException {
    return ((Node.File)handle).resolver.open();
  }
}
