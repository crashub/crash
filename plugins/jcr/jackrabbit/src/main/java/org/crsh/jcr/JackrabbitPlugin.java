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

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;

import javax.jcr.Repository;
import java.util.Map;
import java.util.Properties;

public class JackrabbitPlugin extends JCRPlugin<JackrabbitPlugin> {

  @Override
  public JackrabbitPlugin getImplementation() {
    return this;
  }

  @Override
  public String getName() {
    return "jackrabbit";
  }

  @Override
  public String getDisplayName() {
    return "Jackrabbit JCR plugin";
  }

  @Override
  public String getUsage() {
    return "The command must at least have a URL parameter to be used in connecting to the repository. " +
      "To access a repository via RMI: 'repo use org.apache.jackrabbit" +
            ".repository" +
            ".uri=rmi://localhost:1099/jackrabbit'\n" +
      "To access a repository via JNDI: 'repo use org.apache.jackrabbit" +
            ".repository.uri=jndi:java:comp/env/jcr/jackrabbit'\n" +
      "To access a repository via WebDAV: 'repo use org.apache.jackrabbit" +
              ".repository.uri=http://localhost:8080/jackrabbit/repository/'";
  }

  @Override
  public Repository getRepository(Map<String, String> properties) throws Exception {
    if (properties.containsKey(JcrUtils.REPOSITORY_URI) &&
        "transient".equalsIgnoreCase(properties.get(JcrUtils.REPOSITORY_URI))) {
      Properties props = new Properties();
      for(Map.Entry<String, String> entry : properties.entrySet()) {
        if(!JcrUtils.REPOSITORY_URI.equals(entry.getKey())) {
          props.setProperty(entry.getKey(), entry.getValue());
        }
      }
      return new TransientRepository(props);
    } else {
      Repository repository = JcrUtils.getRepository(properties);
      if (repository != null) {
        return repository;
      }
      return null;
    }
  }
}
