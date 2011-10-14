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

import org.crsh.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PluginManager {

  /** . */
  private final Logger log = LoggerFactory.getLogger(PluginManager.class);

  /** . */
  private final PluginContext context;

  /** . */
  private List<CRaSHPlugin<?>> plugins;

  /** . */
  private PluginDiscovery discovery;

  public PluginManager(PluginContext context, PluginDiscovery discovery) {
    this.context = context;
    this.plugins = null;
    this.discovery = discovery;
  }

  public synchronized <T> Iterable<CRaSHPlugin<?>> getPlugins() {
    if (plugins == null) {
      List<CRaSHPlugin<?>> plugins = Utils.list(discovery.getPlugins());
      for (CRaSHPlugin<?> plugin : plugins) {
        plugin.context = context;
        plugin.status = CRaSHPlugin.CONSTRUCTED;
      }
      this.plugins = plugins;
    }
    return plugins;
  }

  public synchronized <T> Iterable<T> getPlugins(Class<T> wantedType) {

    //
    Iterable<CRaSHPlugin<?>> plugins = getPlugins();

    //
    List<T> tmp = Collections.emptyList();

    //
    for (CRaSHPlugin<?> plugin : plugins) {
      Class<?> pluginType = plugin.getType();
      if (wantedType.isAssignableFrom(pluginType)) {

        switch (plugin.status) {
          default:
          case CRaSHPlugin.FAILED:
          case CRaSHPlugin.INITIALIZED:
            // Do nothing
            break;
          case CRaSHPlugin.CONSTRUCTED:
            int status = CRaSHPlugin.FAILED;
            try {
              plugin.status = CRaSHPlugin.INITIALIZING;
              plugin.init();
              log.info("Initialized plugin " + plugin);
              status = CRaSHPlugin.INITIALIZED;
            }
            catch (Exception e) {
              log.error("Could not initialize plugin " + plugin, e);
            } finally {
              plugin.status = status;
            }
            break;
          case CRaSHPlugin.INITIALIZING:
            throw new RuntimeException("Circular dependency");
        }

        //
        if (plugin.status == CRaSHPlugin.INITIALIZED) {
          if (tmp.isEmpty()) {
            tmp = new ArrayList<T>();
          }
          T t = wantedType.cast(plugin);
          tmp.add(t);
        }
      }
    }

    //
    return tmp;
  }

  public void shutdown() {
    if (plugins != null) {
      for (CRaSHPlugin<?> plugin : plugins) {
        plugin.destroy();
      }
    }
  }
}
