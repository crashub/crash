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
  private CRaSHPlugin<?>[] plugins;

  public PluginManager(PluginContext context) {
    this.context = context;
    this.plugins = null;
  }

  public synchronized <T> Iterable<T> getPlugins(Class<T> pluginType) {
    if (plugins == null) {

      //
      ArrayList<CRaSHPlugin<?>> plugins = new ArrayList<CRaSHPlugin<?>>();
      try {
        ServiceLoader<CRaSHPlugin> loader = ServiceLoader.load(CRaSHPlugin.class, context.getLoader());
        for (CRaSHPlugin<?> plugin : loader) {
          log.info("Loaded plugin " + plugin);
          plugins.add(plugin);
        }
      }
      catch (ServiceConfigurationError e) {
        log.error("Could not load plugins of type " + pluginType, e);
      }

      //
      for (Iterator<CRaSHPlugin<?>> i = plugins.iterator();i.hasNext();) {

        //
        CRaSHPlugin<?> plugin = i.next();

        //
        plugin.context = context;

        //
        try {
          plugin.init();
          log.info("Initialized plugin " + plugin);
        }
        catch (Exception e) {
          i.remove();
          log.error("Could not initialize plugin " + plugin, e);
        }
      }

      //
      this.plugins = plugins.toArray(new CRaSHPlugin<?>[plugins.size()]);
    }

    //
    List<T> tmp = Collections.emptyList();

    //
    for (CRaSHPlugin<?> plugin : plugins) {
      if (plugin.getType().isAssignableFrom(pluginType)) {
        if (tmp.isEmpty()) {
          tmp = new ArrayList<T>();
        }
        T t = pluginType.cast(plugin);
        tmp.add(t);
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
