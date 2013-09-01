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

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpringBootstrap extends PluginLifeCycle implements
    BeanClassLoaderAware,
    BeanFactoryAware,
    InitializingBean,
    DisposableBean {

  /** . */
  private ClassLoader loader;

  /** . */
  private BeanFactory factory;

  public SpringBootstrap() {
  }

  public void setBeanClassLoader(ClassLoader loader) {
    this.loader = loader;
  }

  public void setBeanFactory(BeanFactory factory) throws BeansException {
    this.factory = factory;
  }

  public void afterPropertiesSet() throws Exception {

    // List beans
    Map<String,Object> attributes = new HashMap<String, Object>();
    attributes.put("factory", factory);
    if (factory instanceof ListableBeanFactory) {
      ListableBeanFactory listable = (ListableBeanFactory)factory;
      attributes.put("beans", new SpringMap(listable));
    }

    //
    PluginDiscovery discovery = new SpringPluginDiscovery(loader, factory);

    //
    FS cmdFS = createCommandFS();

    //
    FS confFS = createConfFS();

    //
    PluginContext context = new PluginContext(
        discovery,
        Collections.unmodifiableMap(attributes),
        cmdFS,
        confFS,
        loader);

    //
    context.refresh();

    //
    start(context);
  }

  protected FS createCommandFS() throws IOException, URISyntaxException {
    FS cmdFS = new FS();
    cmdFS.mount(loader, Path.get("/crash/commands/"));
    return cmdFS;
  }

  protected FS createConfFS() throws IOException, URISyntaxException {
    FS confFS = new FS();
    confFS.mount(loader, Path.get("/crash/"));
    return confFS;
  }

  public void destroy() throws Exception {
    stop();
  }
}
