/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.crsh;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

import javax.jcr.Repository;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RepositoryBootstrap
{

   /** . */
   private Repository repository;

   public void bootstrap() throws Exception {

     // JCR configuration
     String containerConf = Thread.currentThread().getContextClassLoader().getResource("conf/standalone/configuration.xml").toString();
     StandaloneContainer.addConfigurationURL(containerConf);

     //
     String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();
     System.setProperty("java.security.auth.login.config", loginConf);

     //
     StandaloneContainer container = StandaloneContainer.getInstance();
     RepositoryService repositoryService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
     repository = repositoryService.getDefaultRepository();
   }

   public Repository getRepository() {
     return repository;
   }

}
