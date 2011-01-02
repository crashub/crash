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

import org.crsh.config.ConfigProperty;
import org.crsh.config.PropertyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PluginLifeCycle implements ServletContextListener {

  /** . */
  private final Logger log = LoggerFactory.getLogger(PluginLifeCycle.class);

  /** . */
  private PluginManager<CRaSHPlugin> manager;

  public void contextInitialized(ServletContextEvent sce) {

/*
    for (PropertyInfo<?> propertyInfo : PropertyInfo.ALL) {
      String value = sce.getServletContext().getInitParameter(propertyInfo.name);
      if (value != null) {
        ConfigProperty<?> property = propertyInfo.toProperty(value);
      }

    }
*/

    //
    manager = new PluginManager<CRaSHPlugin>(Thread.currentThread().getContextClassLoader(), CRaSHPlugin.class);

    // Load plugins
    manager.getPlugins();
  }

  public void contextDestroyed(ServletContextEvent sce) {
  }
}
