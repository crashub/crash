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

package org.crsh.plugin.web;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.vfs.FS;
import org.crsh.vfs.spi.servlet.ServletContextDriver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class WebPluginLifeCycle extends PluginLifeCycle implements ServletContextListener {

  public void contextInitialized(ServletContextEvent sce) {

    //
    FS fs = new FS(new ServletContextDriver(sce.getServletContext(), "/WEB-INF/crash/"));

    //
    PluginContext context = new PluginContext(fs, Thread.currentThread().getContextClassLoader());

    //
    start(context);
  }

  public void contextDestroyed(ServletContextEvent sce) {
    stop();
  }
}
