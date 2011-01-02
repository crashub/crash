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

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.vfs.FS;
import org.crsh.vfs.spi.servlet.ServletContextDriver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class WebPluginLifeCycle extends PluginLifeCycle implements ServletContextListener {

  public void contextInitialized(ServletContextEvent sce) {

    //
    ServletContext sc = sce.getServletContext();

    //
    FS fs = new FS(new ServletContextDriver(sc, "/WEB-INF/crash/"));

    //
    PluginContext context = new PluginContext(fs, Thread.currentThread().getContextClassLoader());

    // Configure from web.xml
    Enumeration<String> names = sc.getInitParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      if (name.startsWith("crash.")) {
        String value = sc.getInitParameter(name).trim();
        String key = name.substring("crash.".length());
        PropertyDescriptor<?> desc = PropertyDescriptor.ALL.get(key);
        context.setProperty(desc, value);
      }
    }

    //
    start(context);
  }

  public void contextDestroyed(ServletContextEvent sce) {
    stop();
  }
}
