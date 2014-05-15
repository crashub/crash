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
package org.crsh.vfs.spi.url;

import org.crsh.vfs.Path;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.Mount;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Julien Viet
 */
public class ClassPathMountFactory implements FSMountFactory<Node> {

  /** . */
  private final ClassLoader loader;

  public ClassPathMountFactory(ClassLoader loader) {
    this.loader = loader;
  }

  @Override
  public Mount<Node> create(Path path) throws IOException {
    if (path == null) {
      throw new NullPointerException();
    }
    URLDriver driver = new URLDriver();
    // Add the resources
    Enumeration<URL> en = loader.getResources(path.getValue().substring(1));
    while (en.hasMoreElements()) {
      URL url = en.nextElement();
      try {
        driver.merge(url);
      }
      catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
    return new Mount<Node>(driver, "classpath:" + path.absolute().getValue());
  }
}
