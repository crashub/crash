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

package org.crsh.vfs.spi.servlet;

import org.crsh.util.Utils;
import org.crsh.vfs.spi.AbstractFSDriver;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServletContextDriver extends AbstractFSDriver<String> {

  /** A valid path. */
  static final Pattern pathPattern = Pattern.compile("^(?=/).*?((?<=/)[^/]*)?(/?)$");

  /** . */
  private final ServletContext ctx;

  /** . */
  private final String root;

  public ServletContextDriver(ServletContext ctx, String root) {
    if (ctx == null) {
      throw new NullPointerException();
    }
    if (root == null) {
      throw new NullPointerException();
    }
    assertMatch(root);

    //
    this.ctx = ctx;
    this.root = root;
  }

  public String root() throws IOException {
    return root;
  }

  public String name(String file) throws IOException {
    return assertMatch(file).group(1);
  }

  public boolean isDir(String file) throws IOException {
    Matcher matcher = assertMatch(file);
    String slash = matcher.group(2);
    return "/".equals(slash);
  }

  public Iterable<String> children(String parent) throws IOException {
    @SuppressWarnings("unchecked")
    Set<String> resourcePaths = (Set<String>)ctx.getResourcePaths(parent);
    return resourcePaths != null ? resourcePaths : Collections.<String>emptyList();
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

  public Iterator<InputStream> open(String handle) throws IOException {
    String realPath = ctx.getRealPath(handle);
    if (realPath != null) {
      File realFile = new File(realPath);
      if (realFile.exists() && realFile.isFile()) {
        return Utils.<InputStream>iterator(new FileInputStream(realFile));
      }
    }
    return Utils.iterator(ctx.getResource(handle).openConnection().getInputStream());
  }

  private Matcher assertMatch(String path) {
    Matcher m = pathPattern.matcher(path);
    if (m.matches()) {
      return m;
    } else {
      throw new IllegalArgumentException("Illegal path " + path);
    }
  }
}
