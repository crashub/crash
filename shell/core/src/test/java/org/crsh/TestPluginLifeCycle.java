/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

package org.crsh;

import org.crsh.plugin.*;
import org.crsh.shell.impl.CRaSH;
import org.crsh.shell.impl.CRaSHSession;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestPluginLifeCycle extends PluginLifeCycle {

  /** . */
  private final PluginContext context;

  /** . */
  private CRaSH crash;

  public TestPluginLifeCycle() throws Exception {
    this(Thread.currentThread().getContextClassLoader());
  }

  public TestPluginLifeCycle(ClassLoader classLoader) throws Exception {
    this(new ServiceLoaderDiscovery(classLoader), classLoader);
  }

  public TestPluginLifeCycle(CRaSHPlugin... plugins) throws Exception {
    this(new SimplePluginDiscovery(plugins), Thread.currentThread().getContextClassLoader());
  }

  private TestPluginLifeCycle(PluginDiscovery discovery, ClassLoader classLoader) throws Exception {
    this.context = new PluginContext(
        discovery,
        new FS().mount(classLoader,Path.get("/crash/commands/")),
        new FS().mount(classLoader,Path.get("/crash/")),
        classLoader);
    this.crash = new CRaSH(context);
  }

  public <T> void setProperty(PropertyDescriptor<T> desc, T value) throws NullPointerException {
    context.setProperty(desc, value);
  }

  public void start() {
    context.refresh();
    start(context);
  }

  public CRaSHSession createShell() {
    return crash.createSession();
  }
}


