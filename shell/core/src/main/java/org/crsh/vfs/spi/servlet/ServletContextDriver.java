/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.vfs.spi.servlet;

import org.crsh.vfs.spi.AbstractFSDriver;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletContextDriver extends AbstractFSDriver<String> {

  /** A valid path. */
  private static final Pattern pathPattern = Pattern.compile("^/.*(?<=/)([^/]+)(/)?$");

  /** . */
  private final ServletContext ctx;

  /** . */
  private final String path;

  public ServletContextDriver(ServletContext ctx, String path) {
    if (ctx == null) {
      throw new NullPointerException();
    }
    if (path == null) {
      throw new NullPointerException();
    }
    if (matcher(path).group(2) == null) {
      throw new IllegalArgumentException("Invalid path " + path);
    }

    //
    this.ctx = ctx;
    this.path = path;
  }

  public String root() throws IOException {
    return path;
  }

  public String name(String file) throws IOException {
    return matcher(file).group(1);
  }

  public boolean isDir(String file) throws IOException {
    Matcher matcher = matcher(file);
    String slash = matcher.group(2);
    return slash != null;
  }

  public Iterable<String> children(String parent) throws IOException {
    return ctx.getResourcePaths(parent);
  }

  public URL toURL(String file) throws IOException {
    return ctx.getResource(file);
  }

  private Matcher matcher(String path) {
    Matcher m = pathPattern.matcher(path);
    if (m.matches()) {
      return m;
    } else {
      throw new IllegalArgumentException("Illegal path " + path);
    }
  }
}
