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
package org.crsh.web.servlet;

import org.crsh.plugin.WebPluginLifeCycle;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.UndeclaredThrowableException;

/** @author Julien Viet */
@WebListener
public class WSLifeCycle extends WebPluginLifeCycle {

  @Override
  protected FS createCommandFS(ServletContext context) {
    try {
      return super.createCommandFS(context).mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/commands/"));
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  @Override
  protected FS createConfFS(ServletContext context) {
    try {
      return super.createConfFS(context).mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }
}
