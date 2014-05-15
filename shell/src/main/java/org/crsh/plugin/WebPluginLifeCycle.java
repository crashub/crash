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
import org.crsh.util.Utils;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.file.FileMountFactory;
import org.crsh.vfs.spi.servlet.WarMountFactory;
import org.crsh.vfs.spi.url.ClassPathMountFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class WebPluginLifeCycle extends Embedded implements ServletContextListener {

  /** . */
  private static final Object lock = new Object();

  /** . */
  private static final Map<String, PluginContext> contextMap = new HashMap<String, PluginContext>();

  /** . */
  private boolean registered = false;

  /** . */
  private Map<String, FSMountFactory<?>> mountContexts = new HashMap<String, FSMountFactory<?>>(3);

  /** . */
  private ServletContext context;

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
   * This implementation register three file system drivers:
   * <ul>
   *   <li><code>file</code> : the current file system</li>
   *   <li><code>classpath</code> : the classpath</li>
   *   <li><code>war</code> : the war content</li>
   * </ul>
   *
   * @return the drivers
   */
  @Override
  protected Map<String, FSMountFactory<?>> getMountFactories() {
    return mountContexts;
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
    context = sce.getServletContext();

    // Use JVM properties as external config
    setConfig(System.getProperties());

    // Initialise the registerable drivers
    try {
      mountContexts.put("classpath", new ClassPathMountFactory(context.getClassLoader()));
      mountContexts.put("file", new FileMountFactory(Utils.getCurrentDirectory()));
      mountContexts.put("war", new WarMountFactory(context));
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Coult not initialize classpath driver", e);
      return;
    }

    //
    String contextPath = context.getContextPath();
    synchronized (lock) {
      if (!contextMap.containsKey(contextPath)) {
        ClassLoader webAppLoader = Thread.currentThread().getContextClassLoader();
        PluginDiscovery discovery = createDiscovery(context, webAppLoader);
        PluginContext pluginContext = start(new ServletContextMap(context), discovery, context.getClassLoader());
        contextMap.put(contextPath, pluginContext);
        registered = true;
      }
    }
  }

  /**
   * The path property is resolved from the servlet context parameters. When the parameter does not exist,
   * the <code>defaultValue</code> argument is used instead, so it should not be null.
   * After the path is resolved, it is interpolated using the JVM system properties and the syntax
   * defined by the {@link org.crsh.util.Utils#interpolate(String, java.util.Map)} function.
   *
   * @param propertyName the property name to resolve
   * @param defaultValue the default property value
   * @return the path value
   */
  private String resolvePathProperty(String propertyName, String defaultValue) {
    String path = context.getInitParameter(propertyName);
    if (path == null) {
      path = defaultValue;
    }
    return Utils.interpolate(path, System.getProperties());
  }

  /**
   * @return the value returned by {@link #resolvePathProperty(String, String)} with the <code>crash.mountpointconfig.conf</code> name
   *         and the {@link #getDefaultConfMountPointConfig()} default value
   */
  @Override
  protected String resolveConfMountPointConfig() {
    return resolvePathProperty("crash.mountpointconfig.conf", getDefaultConfMountPointConfig());
  }

  /**
   * @return the value returned by {@link #resolvePathProperty(String, String)} with the <code>crash.mountpointconfig.cmd</code> name
   *         and the {@link #getDefaultCmdMountPointConfig()} default value
   */
  @Override
  protected String resolveCmdMountPointConfig() {
    return resolvePathProperty("crash.mountpointconfig.cmd", getDefaultCmdMountPointConfig());
  }

  /**
   * @return <code>war:/WEB-INF/crash/commands/</code>
   */
  protected String getDefaultCmdMountPointConfig() {
    return "war:/WEB-INF/crash/commands/";
  }

  /**
   * @return <code>war:/WEB-INF/crash/</code>
   */
  protected String getDefaultConfMountPointConfig() {
    return "war:/WEB-INF/crash";
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
