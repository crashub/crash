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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Resource {

  /** . */
  private final String name;

  /** . */
  private final byte[] content;

  /** . */
  private long timestamp;

  public Resource(String name, byte[] content, long timestamp) {
    this.name = name;
    this.content = content;
    this.timestamp = timestamp;
  }

  public Resource(String name, URL url) throws IOException {
    URLConnection conn = url.openConnection();
    this.name = name;
    this.timestamp = conn.getLastModified();
    this.content = Utils.readAsBytes(conn.getInputStream());
  }

  public String getName() {
    return name;
  }

  public byte[] getContent() {
    return content;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
