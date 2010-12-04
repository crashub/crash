/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.crsh.shell;

import org.crsh.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Resource {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(Resource.class);

  public static Resource create(URL url) {
    try {
      URLConnection conn = url.openConnection();
      long timestamp = conn.getLastModified();
      InputStream in = url.openStream();
      String content = IO.readAsUTF8(in);
      return new Resource(content, timestamp);
    }
    catch (IOException e) {
      log.warn("Could not obtain resource " + url, e);
      return null;
    }
  }

  /** . */
  private final String content;

  /** . */
  private long timestamp;

  public Resource(String content, long timestamp) {
    this.content = content;
    this.timestamp = timestamp;
  }

  public String getContent() {
    return content;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
