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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestShellContext implements ShellContext {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private List<String> scriptPaths;

  public TestShellContext(String... scriptPaths) {

    try {
      Enumeration<URL> en = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).findResources("/");
      System.out.println("Enumerating");
      while (en.hasMoreElements()) {
        URL url = en.nextElement();
        System.out.println("url = " + url);
      }
    }
    catch (IOException e) {
      throw new AssertionError(e);
    }

    this.scriptPaths = new ArrayList<String>(Arrays.asList(scriptPaths));
  }

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
    Resource res = null;
    switch (resourceKind) {
      case LIFECYCLE:
        if ("login".equals(resourceId) || "logout".equals(resourceId)) {
          StringBuilder sb = new StringBuilder();
          long timestamp = Long.MIN_VALUE;
          for (String path : scriptPaths) {
            Resource url = getResource(path + resourceId + ".groovy");
            if (url != null) {
              sb.append(url.getContent());
              timestamp = Math.max(timestamp, url.getTimestamp());
            }
          }
          res = new Resource(sb.toString(), timestamp);
        }
        break;
      case SCRIPT:
        for (String scriptPath : scriptPaths) {
          res = getResource(scriptPath + resourceId + ".groovy");
          if (res != null) {
            break;
          }
        }
        break;
      case CONFIG:
        if ("telnet.properties".equals(resourceId)) {
          res = getResource("crash/telnet/telnet.properties");
        } else {
          resourceId = null;
        }
        break;
      default:
        throw new AssertionError();
    }

    //
    return res;
  }

  public List<String> listResourceId(ResourceKind kind) {
    throw new UnsupportedOperationException();
  }

  private Resource getResource(String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    if (url != null) {
      return Resource.create(url);
    } else {
      return null;
    }
  }

  public ClassLoader getLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
