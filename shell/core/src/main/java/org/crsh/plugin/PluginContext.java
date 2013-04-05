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

import org.crsh.vfs.FS;
import org.crsh.vfs.File;
import org.crsh.vfs.Path;
import org.crsh.vfs.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PluginContext {

  /** . */
  private static final Pattern p = Pattern.compile("(.+)\\.groovy");

  /** . */
  private static final Logger log = Logger.getLogger(PluginContext.class.getName());

  /** . */
  final PluginManager manager;

  /** . */
  private final ClassLoader loader;

  /** . */
  private final String version;

  /** . */
  private final ScheduledExecutorService scanner;

  /** . */
  private final Map<String, Property<?>> properties;

  /** . */
  private final FS cmdFS;

  /** . */
  private final Map<String, Object> attributes;

  /** . */
  private final FS confFS;

  /** The shared executor. */
  private final ExecutorService executor;

  /** . */
  private volatile List<File> dirs;

  /** . */
  private boolean started;

  /** . */
  private ScheduledFuture scannerFuture;


  /**
   * Create a new plugin context with preconfigured executor and scanner, this is equivalent to invoking:
   *
   * <code><pre>new PluginContext(
   *    Executors.newFixedThreadPool(20),
   *    new ScheduledThreadPoolExecutor(1),
   *    discovery,
   *    attributes,
   *    cmdFS,
   *    confFS,
   *    loader);</pre></code>
   *
   * @param discovery the plugin discovery
   * @param cmdFS the command file system
   * @param attributes the attributes
   * @param confFS the conf file system
   * @param loader the loader
   * @throws NullPointerException if any parameter argument is null
   */
  public PluginContext(
      PluginDiscovery discovery,
      Map<String, Object> attributes,
      FS cmdFS,
      FS confFS,
      ClassLoader loader) throws NullPointerException {
    this(
        Executors.newFixedThreadPool(20),
        new ScheduledThreadPoolExecutor(1),
        discovery,
        attributes,
        cmdFS,
        confFS,
        loader);
  }

  /**
   * Create a new plugin context.
   *
   * @param executor the executor for executing asynchronous jobs
   * @param scanner the background scanner for scanning commands
   * @param discovery the plugin discovery
   * @param cmdFS the command file system
   * @param attributes the attributes
   * @param confFS the conf file system
   * @param loader the loader
   * @throws NullPointerException if any parameter argument is null
   */
  public PluginContext(
    ExecutorService executor,
    ScheduledExecutorService scanner,
    PluginDiscovery discovery,
    Map<String, Object> attributes,
    FS cmdFS,
    FS confFS,
    ClassLoader loader) throws NullPointerException {
    if (executor == null) {
      throw new NullPointerException("No null executor accepted");
    }
    if (scanner == null) {
      throw new NullPointerException("No null scanner accepted");
    }
    if (discovery == null) {
      throw new NullPointerException("No null plugin discovery accepted");
    }
    if (confFS == null) {
      throw new NullPointerException("No null configuration file system accepted");
    }
    if (cmdFS == null) {
      throw new NullPointerException("No null command file system accepted");
    }
    if (loader == null) {
      throw new NullPointerException("No null loader accepted");
    }
    if (attributes == null) {
      throw new NullPointerException("No null attributes accepted");
    }

    //
    String version = null;
    try {
      Properties props = new Properties();
      InputStream in = getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.crsh/crsh.shell.core/pom.properties");
      if (in != null) {
        props.load(in);
        version = props.getProperty("version");
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not load maven properties", e);
    }

    //
    if (version == null) {
      log.log(Level.WARNING, "No version found will use unknown value instead");
      version = "unknown";
    }

    //
    this.loader = loader;
    this.attributes = attributes;
    this.version = version;
    this.dirs = Collections.emptyList();
    this.cmdFS = cmdFS;
    this.properties = new HashMap<String, Property<?>>();
    this.started = false;
    this.manager = new PluginManager(this, discovery);
    this.confFS = confFS;
    this.executor = executor;
    this.scanner = scanner;
  }

  public String getVersion() {
    return version;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  /**
   * Returns a context property or null if it cannot be found.
   *
   * @param desc the property descriptor
   * @param <T> the property parameter type
   * @return the property value
   * @throws NullPointerException if the descriptor argument is null
   */
  public <T> T getProperty(PropertyDescriptor<T> desc) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    return getProperty(desc.getName(), desc.getType());
  }

  /**
   * Returns a context property or null if it cannot be found.
   *
   * @param propertyName the name of the property
   * @param type the property type
   * @param <T> the property parameter type
   * @return the property value
   * @throws NullPointerException if the descriptor argument is null
   */
  public <T> T getProperty(String propertyName, Class<T> type) throws NullPointerException {
    if (propertyName == null) {
      throw new NullPointerException("No null property name accepted");
    }
    if (type == null) {
      throw new NullPointerException("No null property type accepted");
    }
    Property<?> property = properties.get(propertyName);
    if (property != null) {
      PropertyDescriptor<?> descriptor = property.getDescriptor();
      if (descriptor.getType().isAssignableFrom(type)) {
        return type.cast(property.getValue());
      }
    }
    return null;
  }

  /**
   * Set a context property to a new value. If the provided value is null, then the property is removed.
   *
   * @param desc the property descriptor
   * @param value the property value
   * @param <T> the property parameter type
   * @throws NullPointerException if the descriptor argument is null
   */
  public <T> void setProperty(PropertyDescriptor<T> desc, T value) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      log.log(Level.FINE, "Removing property " + desc.name);
      properties.remove(desc.getName());
    } else {
      Property<T> property = new Property<T>(desc, value);
      log.log(Level.FINE, "Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc.getName(), property);
    }
  }

  /**
   * Set a context property to a new value. If the provided value is null, then the property is removed.
   *
   * @param desc the property descriptor
   * @param value the property value
   * @param <T> the property parameter type
   * @throws NullPointerException if the descriptor argument is null
   * @throws IllegalArgumentException if the string value cannot be converted to the property type
   */
  public <T> void setProperty(PropertyDescriptor<T> desc, String value) throws NullPointerException, IllegalArgumentException {
    if (desc == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      log.log(Level.FINE, "Removing property " + desc.name);
      properties.remove(desc.getName());
    } else {
      Property<T> property = desc.toProperty(value);
      log.log(Level.FINE, "Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc.getName(), property);
    }
  }

  /**
   * Load a resource from the context.
   *
   * @param resourceId the resource id
   * @param resourceKind the resource kind
   * @return the resource or null if it cannot be found
   */
  public Resource loadResource(String resourceId, ResourceKind resourceKind) {
    Resource res = null;
    try {

      //
      switch (resourceKind) {
        case LIFECYCLE:
          if ("login".equals(resourceId) || "logout".equals(resourceId)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            long timestamp = Long.MIN_VALUE;
            for (File path : dirs) {
              File f = path.child(resourceId + ".groovy", false);
              if (f != null) {
                Resource sub = f.getResource();
                if (sub != null) {
                  buffer.write(sub.getContent());
                  buffer.write('\n');
                  timestamp = Math.max(timestamp, sub.getTimestamp());
                }
              }
            }
            return new Resource(buffer.toByteArray(), timestamp);
          }
          break;
        case COMMAND:
          // Find the resource first, we find for the first found
          for (File path : dirs) {
            File f = path.child(resourceId + ".groovy", false);
            if (f != null) {
              res = f.getResource();
            }
          }
          break;
        case CONFIG:
          String path = "/" + resourceId;
          File file = confFS.get(Path.get(path));
          if (file != null) {
            res = file.getResource();
          }
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not obtain resource " + resourceId, e);
    }
    return res;
  }

  /**
   * List the resources id for a specific resource kind.
   *
   * @param kind the resource kind
   * @return the resource ids
   */
  public List<String> listResourceId(ResourceKind kind) {
    switch (kind) {
      case COMMAND:
        SortedSet<String> all = new TreeSet<String>();
        try {
          for (File path : dirs) {
            for (File file : path.children()) {
              String name = file.getName();
              Matcher matcher = p.matcher(name);
              if (matcher.matches()) {
                all.add(matcher.group(1));
              }
            }
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        all.remove("login");
        all.remove("logout");
        return new ArrayList<String>(all);
      default:
        return Collections.emptyList();
    }
  }

  /**
   * Returns the classloader associated with this context.
   *
   * @return the class loader
   */
  public ClassLoader getLoader() {
    return loader;
  }

  public Iterable<CRaSHPlugin<?>> getPlugins() {
    return manager.getPlugins();
  }

  /**
   * Returns the plugins associated with this context.
   *
   * @param pluginType the plugin type
   * @param <T> the plugin generic type
   * @return the plugins
   */
  public <T> Iterable<T> getPlugins(Class<T> pluginType) {
    return manager.getPlugins(pluginType);
  }

  /**
   * Returns the first plugin associated with this context implementing the specified type.
   *
   * @param pluginType the plugin type
   * @param <T> the plugin generic type
   * @return the plugins
   */
  public <T> T getPlugin(Class<T> pluginType) {
    Iterator<T> plugins = manager.getPlugins(pluginType).iterator();
    return plugins.hasNext() ? plugins.next() : null;
  }

  /**
   * Refresh the fs system view. This is normally triggered by the periodic job but it can be manually
   * invoked to trigger explicit refreshes.
   */
  public void refresh() {
    try {
      File commands = cmdFS.get(Path.get("/"));
      List<File> newDirs = new ArrayList<File>();
      newDirs.add(commands);
      for (File path : commands.children()) {
        if (path.isDir()) {
          newDirs.add(path);
        }
      }
      dirs = newDirs;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  synchronized void start() {
    if (!started) {

      // Start refresh
      Integer refreshRate = getProperty(PropertyDescriptor.VFS_REFRESH_PERIOD);
      TimeUnit timeUnit = getProperty(PropertyDescriptor.VFS_REFRESH_UNIT);
      if (refreshRate != null && refreshRate > 0) {
        TimeUnit tu = timeUnit != null ? timeUnit : TimeUnit.SECONDS;
        scannerFuture = scanner.scheduleWithFixedDelay(new Runnable() {
          public void run() {
            refresh();
          }
        }, 0, refreshRate, tu);
      }

      // Init plugins
      manager.getPlugins(Object.class);

      //
      started = true;
    } else {
      log.log(Level.WARNING, "Attempt to double start");
    }
  }

  synchronized void stop() {

    //
    if (started) {

      // Shutdown manager
      manager.shutdown();

      // Shutdown scanner
      if (scannerFuture != null) {
        scannerFuture.cancel(true);
      }

      //
      scanner.shutdownNow();

      // Shutdown executor
      executor.shutdownNow();
    } else {
      log.log(Level.WARNING, "Attempt to stop when stopped");
    }
  }
}
