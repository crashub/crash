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

package org.crsh.servlet;

import org.crsh.shell.ShellContext;
import org.crsh.term.telnet.TelnetLifeCycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetServletLifeCycle implements ServletContextListener {

  /** . */
  private TelnetLifeCycle lifeCycle;

  /** . */
  private ServletShellContext shellContext;

  public void contextInitialized(ServletContextEvent sce) {
    ServletContext sc = sce.getServletContext();
    ServletShellContext shellContext = new ServletShellContext(sc, Thread.currentThread().getContextClassLoader());
    TelnetLifeCycle lifeCycle = new TelnetLifeCycle(shellContext);
    this.lifeCycle = lifeCycle;
    this.shellContext = shellContext;

    //
    lifeCycle.init();
    shellContext.start();
  }

  public void contextDestroyed(ServletContextEvent sce) {
    if (lifeCycle != null) {
      lifeCycle.destroy();
    }
    if (shellContext != null) {
      shellContext.stop();
    }
  }
}
