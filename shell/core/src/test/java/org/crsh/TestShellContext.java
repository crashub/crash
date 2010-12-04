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

package org.crsh;

import org.crsh.shell.Resource;
import org.crsh.shell.ResourceKind;
import org.crsh.shell.ShellContext;
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
public class TestShellContext implements ShellContext {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  public String getVersion() {
    return "1.0.0";
  }

  public Resource loadResource(String resourceId, ResourceKind resourceKind) {

    if (resourceId == null) {
      throw new NullPointerException("No null resource id");
    }
    if (resourceKind == null) {
      throw new NullPointerException("No null resource kind");
    }

    //
    switch (resourceKind) {
      case LIFECYCLE:
        if ("login".equals(resourceId)) {
          resourceId = "groovy/login.groovy";
        } else if ("logout".equals(resourceId)) {
          resourceId = "groovy/logout.groovy";
        } else {
          resourceId = null;
        }
        break;
      case SCRIPT:
        resourceId = "groovy/commands/" + resourceId + ".groovy";
        break;
      case CONFIG:
        if ("telnet.properties".equals(resourceId)) {
          resourceId = "telnet/telnet.properties";
        } else {
          resourceId = null;
        }
        break;
      default:
        throw new AssertionError();
    }

    //
    Resource res = null;
    if (resourceId != null) {
      try {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceId);
        if (url != null) {
          URLConnection conn = url.openConnection();
          long timestamp = conn.getLastModified();
          InputStream in = url.openStream();
          String content = IO.readAsUTF8(in);
          res = new Resource(content, timestamp);
        }
      } catch (IOException e) {
        log.warn("Could not find resource " + resourceId, e);
      }
    }

    //
    return res;
  }

  public ClassLoader getLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
