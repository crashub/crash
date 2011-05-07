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
package org.crsh.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ServiceLoaderDiscovery implements PluginDiscovery {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

  /** . */
  private final ClassLoader classLoader;

  /**
   * Create a new instance.
   *
   * @param classLoader the loader for the discovery
   * @throws NullPointerException if the loader argument is null
   */
  public ServiceLoaderDiscovery(ClassLoader classLoader) throws NullPointerException {
    if (classLoader == null) {
      throw new NullPointerException();
    }

    //
    this.classLoader = classLoader;
  }

  public Iterable<CRaSHPlugin<?>> getPlugins() {

    //
    ArrayList<CRaSHPlugin<?>> plugins = new ArrayList<CRaSHPlugin<?>>();
    try {
      ServiceLoader<CRaSHPlugin> loader = ServiceLoader.load(CRaSHPlugin.class, classLoader);
      for (CRaSHPlugin<?> plugin : loader) {
        log.info("Loaded plugin " + plugin);
        plugins.add(plugin);
      }
    }
    catch (ServiceConfigurationError e) {
      log.error("Could not load plugins", e);
    }

    //
    return plugins;
  }
}
