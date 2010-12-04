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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PluginLifeCycle implements ServletContextListener
{

   /** . */
   private final Logger log = LoggerFactory.getLogger(PluginLifeCycle.class);

   public void contextInitialized(ServletContextEvent sce)
   {
      ServiceLoader<CRaSHPlugin> loader = ServiceLoader.load(CRaSHPlugin.class, Thread.currentThread().getContextClassLoader());
      for (CRaSHPlugin plugin : loader)
      {
         log.info("Loaded plugin " + plugin);
         try
         {
            plugin.init();
            log.info("Initialized plugin " + plugin);
         }
         catch (Exception e)
         {
            log.error("Could not initilize plugin " + plugin);
         }
      }
   }

   public void contextDestroyed(ServletContextEvent sce)
   {
   }
}
