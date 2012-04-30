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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletContextDriver extends AbstractFSDriver<String> {

  /** A valid path. */
  static final Pattern pathPattern = Pattern.compile("^(?=/).*?((?<=/)[^/]*)?(/?)$");

  /** . */
  private final ServletContext ctx;

  public ServletContextDriver(ServletContext ctx) {
    if (ctx == null) {
      throw new NullPointerException();
    }

    //
    this.ctx = ctx;
  }

  public String root() throws IOException {
    return "/";
  }

  public String name(String file) throws IOException {
    return matcher(file).group(1);
  }

  public boolean isDir(String file) throws IOException {
    Matcher matcher = matcher(file);
    String slash = matcher.group(2);
    return "/".equals(slash);
  }

  public Iterable<String> children(String parent) throws IOException {
    return ctx.getResourcePaths(parent);
  }

  /**
   * The implementation attempts to get an URL that will be valid for the file system first (when the
   * war is usually exploded) and if it is not able, it will delegate to {@link ServletContext#getResource(String)}.
   *
   * @param file the file path
   * @return the URL
   * @throws IOException any io exception
   */
  public URL toURL(String file) throws IOException {
    String realPath = ctx.getRealPath(file);
    if (realPath != null) {
      File realFile = new File(realPath);
      if (realFile.exists() && realFile.isFile()) {
        return realFile.toURI().toURL();
      }
    }
    return ctx.getResource(file);
  }

  public long getLastModified(String handle) throws IOException {
    String realPath = ctx.getRealPath(handle);
    if (realPath != null) {
      File realFile = new File(realPath);
      if (realFile.exists() && realFile.isFile()) {
        return realFile.lastModified();
      }
    }
    return ctx.getResource(handle).openConnection().getLastModified();
  }

  public InputStream open(String handle) throws IOException {
    String realPath = ctx.getRealPath(handle);
    if (realPath != null) {
      File realFile = new File(realPath);
      if (realFile.exists() && realFile.isFile()) {
        return new FileInputStream(realFile);
      }
    }
    return ctx.getResource(handle).openConnection().getInputStream();
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
