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

package org.crsh.telnet;

import org.crsh.plugin.*;
import org.crsh.telnet.term.TelnetLifeCycle;
import org.crsh.vfs.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;

public class TelnetPlugin extends CRaSHPlugin<TelnetPlugin> {

  /** . */
  public static final PropertyDescriptor<Integer> TELNET_PORT = PropertyDescriptor.create("telnet.port", 5000, "The telnet port");

  /** . */
  private TelnetLifeCycle lifeCycle;

  @Override
  public TelnetPlugin getImplementation() {
    return this;
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.<PropertyDescriptor<?>>singletonList(TELNET_PORT);
  }

  @Override
  public void init() {
    PluginContext context = getContext();

    //
    Resource config = null;

    //
    URL configURL = TelnetPlugin.class.getResource("/crash/telnet.properties");
    if (configURL != null) {
      try {
        log.log(Level.FINE, "Found embedded telnet config url " + configURL);
        config = new Resource("telnet.properties", configURL);
      }
      catch (IOException e) {
        log.log(Level.FINE, "Could not load embedded telnet config url " + configURL + " will bypass it", e);
      }
    }

    // Override from config if any
    Resource res = getContext().loadResource("telnet.properties", ResourceKind.CONFIG);
    if (res != null) {
      config = res;
      log.log(Level.FINE, "Found telnet config url " + configURL);
    }

    //
    if (configURL == null) {
      log.log(Level.INFO, "Could not boot Telnet due to missing config");
      return;
    }

    //
    TelnetLifeCycle lifeCycle = new TelnetLifeCycle(context);
    lifeCycle.setConfig(config);
    Integer port = context.getProperty(TELNET_PORT);
    if (port == null) {
      port = TELNET_PORT.defaultValue;
    }
    lifeCycle.setPort(port);

    //
    lifeCycle.init();

    //
    this.lifeCycle = lifeCycle;
  }

  @Override
  public void destroy() {
    if (lifeCycle != null) {
      lifeCycle.destroy();
    }
  }
}
