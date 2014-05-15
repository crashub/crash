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
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.crsh.vfs.spi.FSDriver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A boostrap for starting a standalone CRaSH.
 */
public class Bootstrap extends PluginLifeCycle {

  /** . */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** The configuration file system. */
  private final FS confFS;

  /** The command file system. */
  private final FS cmdFS;

  /** The base classloader. */
  private final ClassLoader loader;

  /** The attributes. */
  private Map<String, Object> attributes;

  /**
   * Create a bootstrap instance with the base classloader and an empty and unmodifiable attribute map.
   *
   * @param baseLoader the base classloader crash will use
   * @param confFS the conf file system
   * @param cmdFS the cmd file system
   * @throws NullPointerException if any argument is null
   */
  public Bootstrap(ClassLoader baseLoader, FS confFS, FS cmdFS) throws NullPointerException {
    if (baseLoader == null) {
      throw new NullPointerException("No null base loader accepted");
    }
    if (confFS == null) {
      throw new NullPointerException("No null conf file system accepted");
    }
    if (cmdFS == null) {
      throw new NullPointerException("No null cmd file system accepted");
    }
    this.attributes = Collections.emptyMap();
    this.confFS = confFS;
    this.cmdFS = cmdFS;
    this.loader = new URLClassLoader(new URL[]{}, baseLoader);
  }

  /**
   * Create a bootstrap instance with the base classloader and an empty and unmodifiable attribute map.
   *
   * @param baseLoader the base classloader crash will use
   * @throws NullPointerException if any argument is null
   */
  public Bootstrap(ClassLoader baseLoader) throws NullPointerException {
    this(baseLoader, new FS(), new FS());
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
   * Add a configuration path driver.
   *
   * @param driver the configuration driver
   * @return this bootstrap
   * @throws NullPointerException when the driver is null
   * @throws IOException any io exception
   */
  public Bootstrap addToConfPath(FSDriver<?> driver) throws IOException, NullPointerException {
    if (driver == null) {
      throw new NullPointerException("No null conf driver");
    }
    log.info("Added " + driver + " driver to conf path");
    confFS.mount(driver);
    return this;
  }

  /**
   * Add a configuration path directory.
   *
   * @param path the configuration path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   * @throws IOException any io exception
   */
  public Bootstrap addToConfPath(File path) throws NullPointerException, IOException {
    if (path == null) {
      throw new NullPointerException("No null conf path");
    }
    log.info("Added " + path.getCanonicalPath() + " file to conf path");
    confFS.mount(path);
    return this;
  }

  /**
   * Add a configuration path.
   *
   * @param path the configuration path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   * @throws IOException any io exception
   * @throws URISyntaxException any uri syntax exception
   */
  public Bootstrap addToConfPath(Path path) throws NullPointerException, IOException, URISyntaxException {
    if (path == null) {
      throw new NullPointerException("No null conf path");
    }
    log.info("Added " + path.getValue() + " path to conf path");
    confFS.mount(loader, path);
    return this;
  }

  /**
   * Add a command path driver.
   *
   * @param driver the command driver
   * @return this bootstrap
   * @throws NullPointerException when the driver is null
   * @throws IOException any io exception
   */
  public Bootstrap addToCmdPath(FSDriver<?> driver) throws IOException, NullPointerException {
    if (driver == null) {
      throw new NullPointerException("No null conf driver");
    }
    log.info("Added " + driver + " driver to command path");
    cmdFS.mount(driver);
    return this;
  }

  /**
   * Add a command path directory.
   *
   * @param path the command path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   * @throws IOException any io exception
   */
  public Bootstrap addToCmdPath(File path) throws NullPointerException, IOException {
    if (path == null) {
      throw new NullPointerException("No null command path");
    }
    log.info("Added " + path.getAbsolutePath() + " file to command path");
    cmdFS.mount(path);
    return this;
  }

  /**
   * Add a command path directory.
   *
   * @param path the command path
   * @return this bootstrap
   * @throws NullPointerException when the path argument is null
   * @throws IOException any io exception
   * @throws URISyntaxException any uri syntax exception
   */
  public Bootstrap addToCmdPath(Path path) throws NullPointerException, IOException, URISyntaxException {
    if (path == null) {
      throw new NullPointerException("No null command path");
    }
    log.info("Added " + path.getValue() + " path to command path");
    cmdFS.mount(loader, path);
    return this;
  }

  /**
   * Trigger the boostrap.
   *
   * @throws Exception any exception that would prevent the bootstrap
   */
  public void bootstrap() throws Exception {

    // The service loader discovery
    ServiceLoaderDiscovery discovery = new ServiceLoaderDiscovery(loader);

    //
    PluginContext context = new PluginContext(
      discovery,
      attributes,
      cmdFS,
      confFS,
      loader);

    //
    context.refresh();

    //
    start(context);
  }

  public void shutdown() {
    stop();
  }
}
