/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.standalone;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Bootstrap extends PluginLifeCycle {

  /** . */
  private final ClassLoader classLoader;

  /** . */
  private PluginDiscovery discovery;

  /** . */
  private FS fileSystem;

  public Bootstrap(ClassLoader classLoader) throws Exception {

    //
    FS fs = new FS();
    fs.mount(new java.io.File("crash"));
    fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));

    //
    this.fileSystem = fs;
    this.classLoader = classLoader;
    this.discovery = new ServiceLoaderDiscovery(classLoader);
  }

  public PluginDiscovery getDiscovery() {
    return discovery;
  }

  public void setDiscovery(PluginDiscovery discovery) {
    this.discovery = discovery;
  }

  public FS getFileSystem() {
    return fileSystem;
  }

  public void setFileSystem(FS fileSystem) {
    this.fileSystem = fileSystem;
  }

  public void bootstrap() throws Exception {
    PluginContext context = new PluginContext(discovery, fileSystem, classLoader);
    context.refresh();
    start(context);
  }

  public void shutdown() {
    stop();
  }
}
