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

package org.crsh.spring;

import org.crsh.vfs.FS;
import org.crsh.vfs.spi.servlet.ServletContextDriver;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URISyntaxException;

public class SpringWebBootstrap extends SpringBootstrap implements ServletContextAware {

  /** . */
  private ServletContext servletContext;

  @Override
  protected FS createCommandFS() throws IOException, URISyntaxException {
    FS commandFS = super.createCommandFS();
    if (servletContext != null) {
      commandFS.mount(new ServletContextDriver(servletContext, "/WEB-INF/crash/commands/"));
    }
    return commandFS;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
