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
package org.crsh.plugin;

import org.crsh.vfs.FS;
import org.crsh.vfs.spi.FSMountFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Julien Viet
 */
public class Embedded extends PluginLifeCycle {

  /**
   * Create the plugin context, allow subclasses to customize it.
   *
   * @param discovery the plugin discovery
   * @return the plugin context
   */
  protected PluginContext create(Map<String, Object> attributes, PluginDiscovery discovery, ClassLoader loader) {

    //
    FS cmdFS;
    FS confFS;
    try {
      cmdFS = createCommandFS();
      confFS = createConfFS();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Coult not initialize the file system", e);
      return null;
    }

    //
    return new PluginContext(discovery, attributes, cmdFS, confFS, loader);
  }

  /**
   * Create and start the plugin context.
   *
   * @param discovery the plugin discovery
   * @return the plugin context
   */
  protected PluginContext start(Map<String, Object> attributes, PluginDiscovery discovery, ClassLoader loader) {
    PluginContext context = create(attributes, discovery, loader);
    if (context != null) {
      context.refresh();
      start(context);
    }
    return context;
  }

  /**
   * Create the command file system from the <code>crash.mountpointconfig.cmd</code> servlet context parameter.
   *
   * @return the command file system
   */
  protected FS createCommandFS() throws IOException {
    return createFS(resolveCmdMountPointConfig());
  }

  /**
   * Create the conf file system from the <code>crash.mountpointconfig.conf</code> servlet context parameter.
   *
   * @return the conf file system
   */
  protected FS createConfFS() throws IOException {
    return createFS(resolveConfMountPointConfig());
  }

  /**
   * @return the registered drivers, by default an empty map is returned, subclasses can override to customize
   */
  protected Map<String, FSMountFactory<?>> getMountFactories() {
    return Collections.emptyMap();
  }

  /**
   * Create a new file system, configured by a the argument <code>mountPointConfig</code>: when the mount point
   * configuration is not null, it is mounted on the returned file system.
   *
   * @param mountPointConfig the mount point configuration
   * @return the configured file system
   * @throws IOException any io exception
   */
  protected FS createFS(String mountPointConfig) throws IOException {
    FS.Builder builder = new FS.Builder();
    for (Map.Entry<String, FSMountFactory<?>> driver : getMountFactories().entrySet()) {
      builder.register(driver.getKey(), driver.getValue());
    }
    if (mountPointConfig != null) {
      builder.mount(mountPointConfig);
    }
    return builder.build();
  }

  protected String resolveConfMountPointConfig() {
    return null;
  }

  protected String resolveCmdMountPointConfig() {
    return null;
  }
}
