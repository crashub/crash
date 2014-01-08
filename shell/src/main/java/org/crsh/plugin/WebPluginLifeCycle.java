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

import org.crsh.util.ServletContextMap;
import org.crsh.vfs.FS;
import org.crsh.vfs.spi.servlet.ServletContextDriver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

public class WebPluginLifeCycle extends PluginLifeCycle implements ServletContextListener {

  /** . */
  private static final Object lock = new Object();

  /** . */
  private static final Map<String, PluginContext> contextMap = new HashMap<String, PluginContext>();

  /** . */
  private boolean registered = false;

  /**
   * Returns a plugin context associated with the servlet context or null if such context does not exist.
   *
   * @param contextPath the context path
   * @return the associated plugin context
   * @throws NullPointerException if the servlet context argument is null
   */
  public static PluginContext getPluginContext(String contextPath) throws NullPointerException {
    synchronized (lock) {
      return contextMap.get(contextPath);
    }
  }

  /**
   * Create the service loader discovery, this can be subclassed to provide an implementation, the current
   * implementation returns a {@link ServiceLoaderDiscovery} instance.
   *
   * @param context the servlet context
   * @param classLoader the class loader
   * @return the plugin discovery
   */
  protected PluginDiscovery createDiscovery(ServletContext context, ClassLoader classLoader) {
    return new ServiceLoaderDiscovery(classLoader);
  }

  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    String contextPath = context.getContextPath();

    // Use JVM properties as external config
    setConfig(System.getProperties());

    //
    synchronized (lock) {
      if (!contextMap.containsKey(contextPath)) {

        //
        FS cmdFS = createCommandFS(context);
        FS confFS = createConfFS(context);
        ClassLoader webAppLoader = Thread.currentThread().getContextClassLoader();
        PluginDiscovery discovery = createDiscovery(context, webAppLoader);

        //
        PluginContext pluginContext = createPluginContext(context, cmdFS, confFS, discovery);

        //
        contextMap.put(contextPath, pluginContext);
        registered = true;

        //
        start(pluginContext);
      }
    }
  }

  /**
   * Create the plugin context, allowing subclasses to provide a custom configuration.
   *
   * @param context the servlet context
   * @param cmdFS the command file system
   * @param confFS the conf file system
   * @param discovery the plugin discovery
   * @return the plugin context
   */
  protected PluginContext createPluginContext(ServletContext context, FS cmdFS, FS confFS, PluginDiscovery discovery) {
    return new PluginContext(discovery, new ServletContextMap(context), cmdFS, confFS, context.getClassLoader());
  }

  /**
   * Create the command file system, this method binds the <code>/WEB-INF/crash/commands/</code> path of the
   * servlet context.
   *
   * @param context the servlet context
   * @return the command file system
   */
  protected FS createCommandFS(ServletContext context) {
    return new FS().mount(new ServletContextDriver(context, "/WEB-INF/crash/commands/"));
  }

  /**
   * Create the conf file system, this method binds the <code>/WEB-INF/crash/</code> path of the
   * servlet context.
   *
   * @param context the servlet context
   * @return the conf file system
   */
  protected FS createConfFS(ServletContext context) {
    return new FS().mount(new ServletContextDriver(context, "/WEB-INF/crash/"));
  }

  public void contextDestroyed(ServletContextEvent sce) {
    if (registered) {

      //
      ServletContext sc = sce.getServletContext();
      String contextPath = sc.getContextPath();

      //
      synchronized (lock) {

        //
        contextMap.remove(contextPath);
        registered = false;

        //
        stop();
      }
    }
  }
}
