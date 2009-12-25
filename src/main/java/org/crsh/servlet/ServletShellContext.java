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
import org.crsh.util.IO;

import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletShellContext implements ShellContext {

  /** . */
  private final ServletContext servletContext;

  /** . */
  private final ClassLoader loader;

  public ServletShellContext(ServletContext servletContext, ClassLoader loader) {
    if (servletContext == null) {
      throw new NullPointerException();
    }
    if (loader == null) {
      throw new NullPointerException();
    }

    //
    this.servletContext = servletContext;
    this.loader = loader;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public String loadScript(String scriptURI) {
    InputStream in = servletContext.getResourceAsStream("/WEB-INF/groovy/" + scriptURI);
    return in != null ? IO.readAsUTF8(in) : null;
  }

  public ClassLoader getLoader() {
    return loader;
  }
}
