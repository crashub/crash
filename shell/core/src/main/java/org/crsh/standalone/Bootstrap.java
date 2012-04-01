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

package org.crsh.standalone;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.util.Utils;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Bootstrap extends PluginLifeCycle {

  /** . */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** The mounted path on the file system. */
  private List<File> mounts = Utils.newArrayList();

  /** The class path. */
  private List<File> classPath = Utils.newArrayList();

  /** The base classloader. */
  private ClassLoader baseLoader;

  public Bootstrap(ClassLoader baseLoader) throws NullPointerException {
    if (baseLoader == null) {
      throw new NullPointerException("No null base loader accepted");
    }
    this.baseLoader = baseLoader;
  }

  public Bootstrap addToMounts(File file) {
    mounts.add(file);
    return this;
  }

  public Bootstrap addToClassPath(File file) {
    classPath.add(file);
    return this;
  }

  public void bootstrap() throws Exception {

    // Compute the url classpath
    URL[] urls = new URL[classPath.size()];
    for (int i = 0;i < urls.length;i++) {
      urls[i] = classPath.get(i).toURI().toURL();
    }

    // Create the classloader
    URLClassLoader classLoader = new URLClassLoader(urls, baseLoader);

    // Create the virtual file system
    FS fs = new FS();
    for (File file : mounts) {
      fs.mount(file);
    }
    fs.mount(classLoader, Path.get("/crash/"));

    // The service loader discovery
    ServiceLoaderDiscovery discovery = new ServiceLoaderDiscovery(classLoader);

    //
    StringBuilder info = new StringBuilder("Booting crash with classpath=");
    info.append(classPath).append(" and mounts=[");
    for (int i = 0;i < mounts.size();i++) {
      if (i > 0) {
        info.append(',');
      }
      info.append(mounts.get(i).getAbsolutePath());
    }
    info.append(']');
    log.info(info.toString());

    //
    PluginContext context = new PluginContext(discovery, fs, classLoader);
    context.refresh();
    start(context);
  }

  public void shutdown() {
    stop();
  }
}
