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
package org.crsh.jcr;

import java.util.Map;
import javax.jcr.Repository;
import org.apache.jackrabbit.commons.JcrUtils;

/**
 * @author <a href="mailto:emmanuel.hugonnet@silverpeas.com">Emmanuel Hugonnet</a>
 * @version $Revision$
 */
public class JackrabbitPlugin extends JCRPlugin<JackrabbitPlugin> {

  @Override
  public JackrabbitPlugin getImplementation() {
    return this;
  }
  
  @Override
  public Repository getRepository(Map<String, String> properties) throws Exception {
    Repository repository = JcrUtils.getRepository(properties);
    if (repository != null) {
      return repository;
    }
    return null;
  }
}
