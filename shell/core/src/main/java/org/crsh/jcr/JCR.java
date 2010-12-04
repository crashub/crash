/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JCR {

  public static Repository getRepository(Map<String, String> properties) throws Exception {

    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    // Get top container
    Class<?> eXoContainerContextClass = cl.loadClass("org.exoplatform.container.ExoContainerContext");
    Method getTopContainerMethod = eXoContainerContextClass.getMethod("getTopContainer");
    Object topContainer = getTopContainerMethod.invoke(null);

    //
    if (topContainer != null) {
      String containerName = properties.get("exo.container.name");
      Object container;
      if (containerName != null) {
        Method getPortalContainerMethod = topContainer.getClass().getMethod("getPortalContainer", String.class);
        container = getPortalContainerMethod.invoke(topContainer, containerName);
      } else {
        container = topContainer;
      }

      //
      if (container != null) {
        Method getComponentInstanceOfTypeMethod = container.getClass().getMethod("getComponentInstanceOfType", Class.class);
        Class<?> repositoryServiceClass = Thread.currentThread().getContextClassLoader().loadClass("org.exoplatform.services.jcr.RepositoryService");
        Object repositoryService = getComponentInstanceOfTypeMethod.invoke(container, repositoryServiceClass);

        //
        if (repositoryService != null) {
          Method getDefaultRepositoryMethod = repositoryService.getClass().getMethod("getDefaultRepository");
          return (Repository)getDefaultRepositoryMethod.invoke(repositoryService);
        }
      }
    }

    //
    return null;
  }
}
