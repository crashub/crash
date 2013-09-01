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

import java.util.Collections;
import java.util.LinkedHashSet;

public class SimplePluginDiscovery implements PluginDiscovery {

  /** . */
  private final LinkedHashSet<CRaSHPlugin<?>> plugins;

  public SimplePluginDiscovery() {
    this.plugins = new LinkedHashSet<CRaSHPlugin<?>>();
  }

  public SimplePluginDiscovery(CRaSHPlugin<?>... plugins) {
    this();
    for (CRaSHPlugin<?> plugin : plugins) {
      add(plugin);
    }
  }
  /**
   * Add a plugin.
   *
   * @param plugin the plugin
   * @return this object
   * @throws NullPointerException if the plugin is null
   */
  public SimplePluginDiscovery add(CRaSHPlugin<?> plugin) throws NullPointerException {
    if (plugin == null) {
      throw new NullPointerException();
    }
    plugins.add(plugin);
    return this;
  }

  public Iterable<CRaSHPlugin<?>> getPlugins() {
    return Collections.unmodifiableSet(plugins);
  }
}
