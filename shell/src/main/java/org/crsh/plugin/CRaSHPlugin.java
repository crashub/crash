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

import org.crsh.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class CRaSHPlugin<P> {

  /** . */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** . */
  public static final int FAILED = -1;

  /** . */
  public static final int CONSTRUCTED = 0;

  /** . */
  public static final int INITIALIZING = 1;

  /** . */
  public static final int INITIALIZED = 2;

  /** . */
  PluginContext context;

  /** . */
  int status;

  /** . */
  private final Class<P> type;

  /** . */
  private List<PropertyDescriptor<?>> configurationCapabilities;

  protected CRaSHPlugin() {
    this.type = (Class<P>)Utils.resolveToClass(getClass(), CRaSHPlugin.class, 0);
    this.status = CONSTRUCTED;
    this.context = null;
  }

  protected final PluginContext getContext() {
    return context;
  }

  /**
   * Returns the current plugin status.
   *
   * @return the plugin status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Returns the plugin type.
   *
   * @return the plugin type
   */
  public final Class<P> getType() {
    return type;
  }

  /**
   * Returns a list of {@link PropertyDescriptor} this plugin requires for its configuration.
   *
   * @return the configuration capabilities
   */
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of {@link PropertyDescriptor} this plugin requires for its configuration.
   *
   * @return the configuration capabilities
   */
  public final Iterable<PropertyDescriptor<?>> getConfigurationCapabilities() {
    if (configurationCapabilities == null) {
      List<PropertyDescriptor<?>> configurationCapabilities = Collections.emptyList();
      for (PropertyDescriptor<?> pd : createConfigurationCapabilities()) {
        if (configurationCapabilities.isEmpty()) {
          configurationCapabilities = new ArrayList<PropertyDescriptor<?>>();
        }
        configurationCapabilities.add(pd);
      }
      this.configurationCapabilities = configurationCapabilities.isEmpty() ? configurationCapabilities : Collections.unmodifiableList(configurationCapabilities);
    }
    return configurationCapabilities;
  }

  /**
   * Returns the implementation.
   *
   * @return the implementation
   */
  public abstract P getImplementation();

  /**
   * Implement this method to know about init life cycle callback.
   */
  public void init() throws Exception {
  }

  /**
   * Implement this method to know about destroy life cycle callback.
   */
  public void destroy() {
  }

  @Override
  public String toString() {
    return "Plugin[type=" + getClass().getSimpleName() + ",interface=" + type.getSimpleName() + "]";
  }
}
