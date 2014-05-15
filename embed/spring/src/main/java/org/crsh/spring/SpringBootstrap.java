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

import org.crsh.plugin.Embedded;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.util.Utils;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.file.FileMountFactory;
import org.crsh.vfs.spi.url.ClassPathMountFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SpringBootstrap extends Embedded implements
    BeanClassLoaderAware,
    BeanFactoryAware,
    InitializingBean,
    DisposableBean {

  /** . */
  private ClassLoader loader;

  /** . */
  private BeanFactory factory;

  /** . */
  protected final HashMap<String, FSMountFactory<?>> drivers = new HashMap<String, FSMountFactory<?>>();

  /** . */
  private String cmdMountPointConfig;

  /** . */
  private String confMountPointConfig;

  public SpringBootstrap() {
  }

  public String getCmdMountPointConfig() {
    return cmdMountPointConfig;
  }

  public void setCmdMountPointConfig(String cmdMountPointConfig) {
    this.cmdMountPointConfig = cmdMountPointConfig;
  }

  public String getConfMountPointConfig() {
    return confMountPointConfig;
  }

  public void setConfMountPointConfig(String confMountPointConfig) {
    this.confMountPointConfig = confMountPointConfig;
  }

  public void setBeanClassLoader(ClassLoader loader) {
    this.loader = loader;
  }

  public void setBeanFactory(BeanFactory factory) throws BeansException {
    this.factory = factory;
  }

  public void afterPropertiesSet() throws Exception {

    // Initialise the registerable drivers
    try {
      drivers.put("classpath", new ClassPathMountFactory(loader));
      drivers.put("file", new FileMountFactory(Utils.getCurrentDirectory()));
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Coult not initialize classpath driver", e);
      return;
    }

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
    start(Collections.unmodifiableMap(attributes), discovery, loader);
  }

  @Override
  protected Map<String, FSMountFactory<?>> getMountFactories() {
    return drivers;
  }

  @Override
  protected String resolveConfMountPointConfig() {
    return confMountPointConfig != null ? confMountPointConfig : getDefaultConfMountPointConfig();
  }

  @Override
  protected String resolveCmdMountPointConfig() {
    return cmdMountPointConfig != null ? cmdMountPointConfig : getDefaultCmdMountPointConfig();
  }

  protected String getDefaultCmdMountPointConfig() {
    return "classpath:/crash/commands/";
  }

  protected String getDefaultConfMountPointConfig() {
    return "classpath:/crash/";
  }

  public void destroy() throws Exception {
    stop();
  }
}
