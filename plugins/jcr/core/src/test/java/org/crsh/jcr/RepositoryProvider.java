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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public abstract class RepositoryProvider
{

  /** . */
  private static RepositoryProvider provider;

  /** . */
  private static boolean initialized;

  public static synchronized RepositoryProvider getProvider() throws Exception {
    if (!initialized) {
      Iterator<RepositoryProvider> it = ServiceLoader.load(RepositoryProvider.class).iterator();
      if (it.hasNext()) {
        RepositoryProvider repoBoostrap = it.next();
        repoBoostrap.bootstrap();
        provider = repoBoostrap;
        initialized = true;
      } else {
        throw new NoSuchElementException("No repository available");
      }
    }
    return provider;
  }

  protected abstract void bootstrap() throws Exception;

  public abstract Repository getRepository();

  public abstract String getLogin() throws Exception;

  public abstract void logout() throws Exception;

}
