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
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PluginManager<P extends CRaSHPlugin> {

  /** . */
  private final Logger log = LoggerFactory.getLogger(PluginManager.class);

  /** . */
  private final PluginContext context;

  /** . */
  private List<P> plugins;

  /** . */
  private final Class<P> pluginType;

  public PluginManager(PluginContext context, Class<P> pluginType) {
    this.context = context;
    this.pluginType = pluginType;
    this.plugins = null;
  }

  public synchronized Iterable<P> getPlugins() {
    if (plugins == null) {

      //
      ArrayList<P> plugins = new ArrayList<P>();
      try {
        ServiceLoader<P> loader = ServiceLoader.load(pluginType, context.getLoader());
        for (P plugin : loader) {
          log.info("Loaded plugin " + plugin);
          plugins.add(plugin);
        }
      }
      catch (ServiceConfigurationError e) {
        log.error("Could not load plugins of type " + pluginType, e);
      }

      //
      for (Iterator<P> i = plugins.iterator();i.hasNext();) {

        //
        P plugin = i.next();

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
      this.plugins = plugins;
    }

    //
    return plugins;
  }

  public void shutdown() {
    for (CRaSHPlugin plugin : plugins) {
      plugin.destroy();
    }
  }
}
