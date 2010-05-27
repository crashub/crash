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

package org.crsh.term.telnet;

import net.wimpi.telnetd.TelnetD;
import org.crsh.term.CRaSHLifeCycle;
import org.crsh.shell.ShellContext;

import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetLifeCycle extends CRaSHLifeCycle {

  /** . */
  private TelnetD daemon;

  /** . */
  static TelnetLifeCycle instance;

  public TelnetLifeCycle(ShellContext context) {
    super(context);
  }

  @Override
  protected synchronized void doInit() throws Exception {
    if (instance != null) {
      throw new IllegalStateException("An instance already exists");
    }

    //
    String s = getShellContext().loadResource("/telnet/telnet.properties").getContent();
    Properties props = new Properties();
    props.load(new ByteArrayInputStream(s.getBytes("ISO-8859-1")));
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
