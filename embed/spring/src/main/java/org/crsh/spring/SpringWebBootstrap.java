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

import org.crsh.vfs.spi.servlet.WarMountFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

public class SpringWebBootstrap extends SpringBootstrap implements ServletContextAware {

  /** . */
  private ServletContext servletContext;

  @Override
  public void afterPropertiesSet() throws Exception {

    // Configure the war driver
    drivers.put("war", new WarMountFactory(servletContext));

    //
    super.afterPropertiesSet();
  }

  @Override
  protected String getDefaultCmdMountPointConfig() {
    return "war:/WEB-INF/crash/commands/";
  }

  @Override
  protected String getDefaultConfMountPointConfig() {
    return "war:/WEB-INF/crash/";
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
