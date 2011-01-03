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

package org.crsh.telnet.term;

import net.wimpi.telnetd.TelnetD;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.term.CRaSHLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetLifeCycle extends CRaSHLifeCycle {

  /** . */
  private final Logger log = LoggerFactory.getLogger(TelnetLifeCycle.class);

  /** . */
  private TelnetD daemon;

  /** . */
  static TelnetLifeCycle instance;

  /** . */
  private Integer port;

  public TelnetLifeCycle(PluginContext context) {
    super(context);
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  @Override
  protected synchronized void doInit() throws Exception {
    if (instance != null) {
      throw new IllegalStateException("An instance already exists");
    }

    //
    String s = getShellContext().loadResource("telnet.properties", ResourceKind.CONFIG).getContent();
    Properties props = new Properties();
    props.load(new ByteArrayInputStream(s.getBytes("ISO-8859-1")));

    //
    if (port != null) {
      log.debug("Explicit telnet port configuration with value " + port);
      props.put("std.port", port.toString());
    } else {
      log.debug("Use default telnet port configuration " + props.getProperty("std.port"));
    }

    //
    TelnetD daemon = TelnetD.createTelnetD(props);
    daemon.start();

    //
    this.daemon = daemon;
    instance = this;
  }

  @Override
  protected synchronized void doDestroy() {
    instance = null;
    if (daemon != null) {
      daemon.stop();
      daemon = null;
    }
  }
}
