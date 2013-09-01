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

import org.crsh.util.Utils;
import org.crsh.vfs.spi.AbstractFSDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class URLDriver extends AbstractFSDriver<Node> {

  /** . */
  private final Node root;

  public URLDriver() {
    this.root = new Node();
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
    return handle.files.isEmpty();
  }

  public Iterable<Node> children(Node handle) throws IOException {
    return handle.children.values();
  }

  public long getLastModified(Node handle) throws IOException {
    return handle.files.isEmpty() ? 0 : handle.files.peekFirst().lastModified;
  }

  public Iterator<InputStream> open(Node handle) throws IOException {
    ArrayList<InputStream> list = new ArrayList<InputStream>(handle.files.size());
    for (Node.File file : handle.files) {
      list.add(file.resolver.open());
    }
    return list.iterator();
  }
}
