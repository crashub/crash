/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The plugin context.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PluginContext {

  /** . */
  private static final Pattern p = Pattern.compile("(.+)\\.groovy");

  /** . */
  private static final Logger log = LoggerFactory.getLogger(PluginContext.class);

  /** . */
  final PluginManager manager;

  /** . */
  private final ClassLoader loader;

  /** . */
  private final String version;

  /** . */
  private ScheduledExecutorService executor;

  /** . */
  private volatile List<File> dirs;

  /** . */
  private final Map<PropertyDescriptor<?>, Property<?>> properties;

  /** . */
  private final FS vfs;

  /** . */
  private boolean started;

  /**
   * Create a new plugin context.
   *
   * @param discovery the plugin discovery
   * @param fs the file system
   * @param loader the loader
   * @throws NullPointerException if any parameter argument is null
   */
  public PluginContext(PluginDiscovery discovery, FS fs, ClassLoader loader) throws NullPointerException {
    if (fs == null) {
      throw new NullPointerException();
    }
    if (loader == null) {
      throw new NullPointerException();
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
      log.error("Could not load maven properties", e);
    }

    //
    if (version == null) {
      log.warn("No version found will use unknown value instead");
      version = "unknown";
    }

    //
    this.loader = loader;
    this.version = version;
    this.dirs = Collections.emptyList();
    this.vfs = fs;
    this.properties = new HashMap<PropertyDescriptor<?>, Property<?>>();
    this.started = false;
    this.manager = new PluginManager(this, discovery);
  }

  public final String getVersion() {
    return version;
  }

  /**
   * Returns a context property or null if it cannot be found.
   *
   * @param desc the property descriptor
   * @param <T> the property parameter type
   * @return the property value
   * @throws NullPointerException if the descriptor argument is null
   */
  public final <T> T getProperty(PropertyDescriptor<T> desc) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    Property<T> property = (Property<T>)properties.get(desc);
    return property != null ? property.getValue() : desc.defaultValue;
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
  public final <T> T getProperty(String propertyName, Class<T> type) throws NullPointerException {
    if (propertyName == null) {
      throw new NullPointerException("No null property name accepted");
    }
    if (type == null) {
      throw new NullPointerException("No null property type accepted");
    }
    for (PropertyDescriptor<?> pd : properties.keySet())
    {
      if (pd.name.equals(propertyName) && type.isAssignableFrom(pd.type))
      {
        return type.cast(getProperty(pd));
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
  public final <T> void setProperty(PropertyDescriptor<T> desc, T value) throws NullPointerException {
    if (desc == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      log.debug("Removing property " + desc.name);
      properties.remove(desc);
    } else {
      Property<T> property = new Property<T>(desc, value);
      log.debug("Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc, property);
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
  public final <T> void setProperty(PropertyDescriptor<T> desc, String value) throws NullPointerException, IllegalArgumentException {
    if (desc == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      log.debug("Removing property " + desc.name);
      properties.remove(desc);
    } else {
      Property<T> property = desc.toProperty(value);
      log.debug("Setting property " + desc.name + " to value " + property.getValue());
      properties.put(desc, property);
    }
  }

  public final Resource loadResource(String resourceId, ResourceKind resourceKind) {
    Resource res = null;
    try {

      //
      switch (resourceKind) {
        case LIFECYCLE:
          if ("login".equals(resourceId) || "logout".equals(resourceId)) {
            StringBuilder sb = new StringBuilder();
            long timestamp = Long.MIN_VALUE;
            for (File path : dirs) {
              File f = path.child(resourceId + ".groovy", false);
              if (f != null) {
                Resource sub = f.getResource();
                if (sub != null) {
                  sb.append(sub.getContent() + "\n");
                  timestamp = Math.max(timestamp, sub.getTimestamp());
                }
              }
            }
            return new Resource(sb.toString(), timestamp);
          }
          break;
        case SCRIPT:
          // Find the resource first, we find for the first found
          for (File path : dirs) {
            File f = path.child(resourceId + ".groovy", false);
            if (f != null) {
              res = f.getResource();
            }
          }
          break;
        case CONFIG:
          if ("telnet.properties".equals(resourceId)) {
            File telnet = vfs.get(Path.get("/telnet/telnet.properties"));
            if (telnet != null) {
              res = telnet.getResource();
            }
          }
          if ("crash.properties".equals(resourceId)) {
            File props = vfs.get(Path.get("/crash.properties"));
            if (props != null) {
              res = props.getResource();
            }
          }
          break;
        case KEY:
          if ("hostkey.pem".equals(resourceId)) {
            File key = vfs.get((Path.get("/ssh/hostkey.pem")));
            if (key != null) {
              res = key.getResource();
            }
          }
          break;
      }
    } catch (IOException e) {
      log.warn("Could not obtain resource " + resourceId, e);
    }
    return res;
  }

  public final List<String> listResourceId(ResourceKind kind) {
    switch (kind) {
      case SCRIPT:
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

  public final ClassLoader getLoader() {
    return loader;
  }

  /**
   * Refresh the fs system view. This is normally triggered by the periodic job but it can be manually
   * invoked to trigger explicit refreshes.
   */
  public final void refresh() {
    try {
      File commands = vfs.get(Path.get("/commands/"));
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

  public final synchronized  void start() {
    if (!started) {

      // Start refresh
      Integer refreshRate = getProperty(PropertyDescriptor.VFS_REFRESH_PERIOD);
      TimeUnit timeUnit = getProperty(PropertyDescriptor.VFS_REFRESH_UNIT);
      if (refreshRate != null && refreshRate > 0) {
        TimeUnit tu = timeUnit != null ? timeUnit : TimeUnit.SECONDS;
        executor =  new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(new Runnable() {
          int count = 0;
          public void run() {
            refresh();
          }
        }, 0, refreshRate, tu);
      }

      // Init services
      manager.getPlugins(Service.class);

      //
      started = true;
    } else {
      log.warn("Attempt to double start");
    }
  }

  public final synchronized void stop() {

    //
    if (started) {

      // Shutdown manager
      manager.shutdown();

      //
      if (executor != null) {
        ScheduledExecutorService tmp = executor;
        executor = null;
        tmp.shutdown();
      }
    } else {
      log.warn("Attempt to stop when stopped");
    }
  }

  public <T> Iterable<T> getPlugins(Class<T> pluginType) {
    return manager.getPlugins(pluginType);
  }
}
