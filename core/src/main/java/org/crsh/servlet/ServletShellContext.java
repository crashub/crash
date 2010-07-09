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
package org.crsh.servlet;

import org.crsh.shell.Resource;
import org.crsh.shell.ShellContext;
import org.crsh.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletShellContext implements ShellContext {

  /** . */
  private final ServletContext servletContext;

  /** . */
  private final ClassLoader loader;

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private final String version;

  public ServletShellContext(ServletContext servletContext, ClassLoader loader) {
    if (servletContext == null) {
      throw new NullPointerException();
    }
    if (loader == null) {
      throw new NullPointerException();
    }

    //
    String version = null;
    try {
      Properties props = new Properties();
      InputStream in = servletContext.getResourceAsStream("/META-INF/maven/org.crsh/crsh.core/pom.properties");
      if (in != null) {
        props.load(in);
        version = props.getProperty("version");
      }
    } catch (Exception e) {
      log.error("Could not load maven properties", e);
    }

    //
    if (version == null) {
      log.warn("No version found will use unknown value instead");
      version = "unkown";
    }

    //
    this.servletContext = servletContext;
    this.loader = loader;
    this.version = version;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public String getVersion() {
    return version;
  }

  public Resource loadResource(String resourceId) {
    Resource res = null;
    try {
      URL url = servletContext.getResource("/WEB-INF/" + resourceId);
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
    return res;
  }

  public ClassLoader getLoader() {
    return loader;
  }
}
