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
package org.crsh.jcr;


import javax.jcr.Repository;

import org.crsh.jcr.ExoPlugin;
import org.crsh.jcr.RepositoryProvider;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

public class ExoRepositoryProvider extends RepositoryProvider {

  /** . */
  private Repository repository;

  public void bootstrap() throws Exception {

    // Initialize groovy integration by JCR plugin
    new ExoPlugin().init();

    // JCR configuration
    String containerConf = Thread.currentThread().getContextClassLoader().getResource("conf/standalone/configuration.xml").toString();
    StandaloneContainer.addConfigurationURL(containerConf);

    //
    String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();
    System.setProperty("java.security.auth.login.config", loginConf);

    //
    StandaloneContainer container = StandaloneContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = repositoryService.getDefaultRepository();
  }

  public Repository getRepository() {
    return repository;
  }

  @Override
  public String getLogin() throws Exception {
    return "repo use";
  }

  @Override
  public void logout() throws Exception {
  }
}
