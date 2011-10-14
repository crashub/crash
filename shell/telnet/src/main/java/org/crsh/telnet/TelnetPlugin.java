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

package org.crsh.telnet;

import org.crsh.plugin.*;
import org.crsh.telnet.term.TelnetLifeCycle;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetPlugin extends CRaSHPlugin<TelnetPlugin> implements Service {

  /** . */
  public static final PropertyDescriptor<Integer> TELNET_PORT = new PropertyDescriptor<Integer>(Integer.class, "telnet.port", 5000, "The telnet port") {
    @Override
    public Integer doParse(String s) {
      return Integer.parseInt(s);
    }
  };

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
    TelnetLifeCycle lifeCycle = new TelnetLifeCycle(context);
    Integer port = context.getProperty(TELNET_PORT);
    if (port != null) {
      lifeCycle.setPort(port);
    }

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
