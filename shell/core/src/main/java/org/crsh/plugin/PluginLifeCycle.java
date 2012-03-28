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

import org.crsh.vfs.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Controls the life cycle of a plugin manager.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PluginLifeCycle {

  /** . */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private PluginContext context;

  public PluginContext getContext() {
    return context;
  }

  protected final void start(PluginContext context) throws IllegalStateException {
    if (this.context != null) {
      throw new IllegalStateException("Already started");
    }
    
    // Get properties from system properties
    Properties props = new Properties();

    // Load properties from configuration file
    Resource res = context.loadResource("crash.properties", ResourceKind.CONFIG);
    if (res != null) {
      try {
        URL url = res.getURL();
        InputStream in = url.openStream();
        props.load(in);
        log.debug("Loaded properties from " + url + " " + props);
      } catch (IOException e) {
        log.warn("Could not configure from crash.properties", e);
      }
    } else {
      log.debug("Could not find crash.properties file");
    }

    // Override default properties from System properties
    props.putAll(System.getProperties());

    // Override default properties from command line
    for (PropertyDescriptor<?> desc : PropertyDescriptor.ALL.values()) {
      configureProperty(context, props, desc);
    }

    // Override default properties from plugin defined properties.
    for (final CRaSHPlugin<?> plugin : context.manager.getPlugins())
    {
      for (PropertyDescriptor<?> descriptor : plugin.getConfigurationCapabilities()) {
        configureProperty(context, props, descriptor);
      }
    }

    //
    context.start();

    //
    this.context = context;
  }

  public final void stop() throws IllegalStateException {
    if (context == null) {
      throw new IllegalStateException("Not started");
    }
    PluginContext context = this.context;
    this.context = null;
    context.stop();
  }

  private void configureProperty(PluginContext context, Properties props, PropertyDescriptor<?> desc) {
    String key = "crash." + desc.name;
    String value = props.getProperty(key);
    if (value != null) {
      try {
        log.info("Configuring property " + desc.name + "=" + value + " from properties");
        context.setProperty(desc, value);
      }
      catch (IllegalArgumentException e) {
        log.error("Could not configure property", e);
      }
    }
  }
}
