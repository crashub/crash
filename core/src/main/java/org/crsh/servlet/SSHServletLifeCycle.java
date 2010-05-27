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

import org.crsh.term.sshd.SSHLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHServletLifeCycle implements ServletContextListener {

  /** . */
  private final Logger log = LoggerFactory.getLogger(SSHServletLifeCycle.class);

  /** . */
  private SSHLifeCycle lifeCycle;
  
  public void contextInitialized(ServletContextEvent sce) {

    ServletContext sc = sce.getServletContext();

    // Configuration from web.xml
    final String username = sc.getInitParameter("ssh.username").trim();
    final String password = sc.getInitParameter("ssh.password").trim();
    int port = Integer.parseInt(sc.getInitParameter("ssh.port").trim());
    String keyPath = sc.getInitParameter("ssh.keypath");

    // Use the default one
    if (keyPath == null) {
      log.debug("No key path found in web.xml will use the default one");
      keyPath = sc.getRealPath("/WEB-INF/sshd/hostkey.pem");
      log.debug("Going to use the key path at " + keyPath);
    }

    SSHLifeCycle lifeCycle = new SSHLifeCycle(new ServletShellContext(sc, Thread.currentThread().getContextClassLoader()));

    //
    lifeCycle.setKeyPath(keyPath);
    lifeCycle.setPort(port);
    lifeCycle.setUserName(username);
    lifeCycle.setPassword(password);

    //
    lifeCycle.init();

    //
    this.lifeCycle = lifeCycle;
  }

  public void contextDestroyed(ServletContextEvent sce) {
    if (lifeCycle != null) {
      lifeCycle.destroy();
      lifeCycle = null;
    }
  }
}
