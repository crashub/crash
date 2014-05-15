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
package org.crsh.vfs.spi.servlet;

import org.crsh.vfs.Path;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.Mount;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * @author Julien Viet
 */
public class WarMountFactory implements FSMountFactory<String> {

  /** . */
  private final ServletContext context;

  public WarMountFactory(ServletContext context) {
    this.context = context;
  }

  @Override
  public Mount<String> create(Path path) throws IOException {
    return new Mount<String>(new ServletContextDriver(context, path.absolute().getValue()), "war:" + path.absolute().getValue());
  }
}
