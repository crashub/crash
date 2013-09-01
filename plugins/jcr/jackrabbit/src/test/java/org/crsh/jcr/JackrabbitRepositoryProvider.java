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

import org.apache.jackrabbit.core.TransientRepository;
import org.crsh.jcr.JackrabbitPlugin;
import org.crsh.jcr.RepositoryProvider;

import javax.jcr.Repository;
import java.io.File;

public class JackrabbitRepositoryProvider extends RepositoryProvider {

  /** . */
  private Repository repository;

  public void bootstrap() throws Exception {
    // Initialize groovy integration by JCR plugin
    new JackrabbitPlugin().init();

    // Init
    File config = new File(Thread.currentThread().getContextClassLoader().getResource("conf/transient/").toURI());
    repository = new TransientRepository("repository-in-memory.xml", config.getPath());
  }

  public Repository getRepository() {
    return repository;
  }

  @Override
  public String getLogin() throws Exception {
    File config = new File(Thread.currentThread().getContextClassLoader().getResource("conf/transient/").toURI());
    return "repo use org.apache.jackrabbit.repository.conf=repository-in-memory.xml;org.apache.jackrabbit.repository.home=" + config.getAbsolutePath();
  }

  @Override
  public void logout() throws Exception {
    File config = new File(Thread.currentThread().getContextClassLoader().getResource("conf/transient/").toURI());
    if (config.exists()) {
      config.delete();
    }
  }
}
