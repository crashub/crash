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

package org.crsh.plugin.telnet;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.Property;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.term.spi.telnet.TelnetLifeCycle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetPlugin extends CRaSHPlugin {

  /** . */
  private TelnetLifeCycle lifeCycle;

  @Override
  public void init() {
    PluginContext context = getContext();

    //
    TelnetLifeCycle lifeCycle = new TelnetLifeCycle(context);
    Property<Integer> portProp = context.getProperty(PropertyDescriptor.TELNET_PORT);
    if (portProp != null) {
      lifeCycle.setPort(portProp.getValue());
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
