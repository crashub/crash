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

package org.crsh.standalone;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.util.Utils;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A boostrap for starting a standalone CRaSH.
 */
public class Bootstrap extends PluginLifeCycle {

  /** . */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** The mounted path on the file system. */
  private List<File> cmdpath = Utils.newArrayList();

  /** The mounted path on the file system. */
  private List<File> confpath = Utils.newArrayList();

  /** The base classloader. */
  private ClassLoader baseLoader;

  /** The attributes. */
  private Map<String, Object> attributes;

  /**
   * Create a bootstrap instance with the base classloader and an empty and unmodifiable attribute map.
   *
   * @param baseLoader the base classloader crash will use
   * @throws NullPointerException if the loader argument is null
   */
  public Bootstrap(ClassLoader baseLoader) throws NullPointerException {
    if (baseLoader == null) {
      throw new NullPointerException("No null base loader accepted");
    }
    this.baseLoader = baseLoader;
    this.attributes = Collections.emptyMap();
  }

  /**
   * Replaces the attributes to use, the new attributes map will be used as is and not copied.
   *
   * @param attributes the attribute map
   */
  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  /**
   * Add a conf path directory.
   *
   * @param path the configuration path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   */
  public Bootstrap addToConfPath(File path) throws NullPointerException {
    if (path == null) {
      throw new NullPointerException("No null configuration path");
    }
    confpath.add(path);
    return this;
  }

  /**
   * Add a command path directory.
   *
   * @param path the configuration path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   */
  public Bootstrap addToCmdPath(File path) {
    if (path == null) {
      throw new NullPointerException("No null command path");
    }
    cmdpath.add(path);
    return this;
  }

  /**
   * Trigger the boostrap.
   *
   * @throws Exception any exception that would prevent the bootstrap
   */
  public void bootstrap() throws Exception {

    // Create the classloader from the url classpath
    URLClassLoader classLoader = new URLClassLoader(new URL[]{}, baseLoader);

    // Create the cmd file system
    FS cmdFS = new FS();
    for (File cmd : cmdpath) {
      cmdFS.mount(cmd);
    }

    // Add the classloader
    cmdFS.mount(classLoader, Path.get("/crash/commands/"));

    // Create the conf file system
    FS confFS = new FS();
    for (File conf : confpath) {
      confFS.mount(conf);
    }
    confFS.mount(classLoader, Path.get("/crash/"));

    // The service loader discovery
    ServiceLoaderDiscovery discovery = new ServiceLoaderDiscovery(classLoader);

    //
    StringBuilder info = new StringBuilder("Booting crash with mounts=[");
    for (int i = 0;i < cmdpath.size();i++) {
      if (i > 0) {
        info.append(',');
      }
      info.append(cmdpath.get(i).getAbsolutePath());
    }
    info.append(']');
    log.log(Level.INFO, info.toString());

    //
    PluginContext context = new PluginContext(
      discovery,
      attributes,
      cmdFS,
      confFS,
      classLoader);

    //
    context.refresh();

    //
    start(context);
  }

  public void shutdown() {
    stop();
  }
}
