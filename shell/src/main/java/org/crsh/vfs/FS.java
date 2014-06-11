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

package org.crsh.vfs;

import org.crsh.vfs.spi.FSDriver;
import org.crsh.vfs.spi.FSMountFactory;
import org.crsh.vfs.spi.Mount;
import org.crsh.vfs.spi.file.FileDriver;
import org.crsh.vfs.spi.url.ClassPathMountFactory;
import org.crsh.vfs.spi.url.URLDriver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The file system provides a federated view of {@link org.crsh.vfs.spi.FSDriver} mounts.
 */
public class FS {

  public static class Builder {

    /** . */
    private HashMap<String, FSMountFactory<?>> resolvers;

    /** . */
    private ArrayList<Mount<?>> mounts = new ArrayList<Mount<?>>();

    public Builder() {
      this.resolvers = new HashMap<String, FSMountFactory<?>>();
    }

    /**
     * Register a resolver.
     *
     * @param name the registration name
     * @param resolver the resolver implementation
     */
    public Builder register(String name, FSMountFactory<?> resolver) {
      resolvers.put(name, resolver);
      return this;
    }

    public Builder mount(String name, Path path) throws IOException, IllegalArgumentException {
      FSMountFactory<?> resolver = resolvers.get(name);
      if (resolver == null) {
        throw new IllegalArgumentException("Unknown driver " + name);
      } else {
        Mount<?> mount = resolver.create(path);
        mounts.add(mount);
        return this;
      }
    }

    public Builder mount(String mountPointConfig) throws IOException {
      int prev = 0;
      while (true) {
        int next = mountPointConfig.indexOf(';', prev);
        if (next == -1) {
          next = mountPointConfig.length();
        }
        if (next > prev) {
          String mount = mountPointConfig.substring(prev, next);
          int index = mount.indexOf(':');
          String name;
          String path;
          if (index == -1) {
            name = "classpath";
            path = mount;
          } else {
            name = mount.substring(0, index);
            path = mount.substring(index + 1);
          }
          mount(name, Path.get(path));
          prev = next + 1;
        } else {
          break;
        }
      }
      return this;
    }

    public List<Mount<?>> getMounts() {
      return mounts;
    }

    public FS build() throws IOException {
      FS fs = new FS();
      for (Mount<?> mount : mounts) {
        fs.mount(mount.getDriver());
      }
      return fs;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Iterator<Mount<?>> i = mounts.iterator();i.hasNext();) {
        Mount<?> mount = i.next();
        sb.append(mount.getValue());
        if (i.hasNext()) {
          sb.append(';');
        }
      }
      return sb.toString();
    }
  }

  /** . */
  final List<FSDriver<?>> drivers;

  public FS() {
    this.drivers = new ArrayList<FSDriver<?>>();
  }

  public File get(Path path) throws IOException {
    return new File(this, path);
  }

  public FS mount(FSDriver<?> driver) throws IOException {
    if (driver == null) {
      throw new NullPointerException();
    }
    drivers.add(driver);
    return this;
  }

  public FS mount(java.io.File root) throws IOException {
    return mount(new FileDriver(root));
  }

  public FS mount(ClassLoader cl, Path path) throws IOException, URISyntaxException {
    if (cl == null) {
      throw new NullPointerException();
    } else {
      return mount(new ClassPathMountFactory(cl).create(path).getDriver());
    }
  }

  public FS mount(Class<?> clazz) throws IOException, URISyntaxException {
    if (clazz == null) {
      throw new NullPointerException();
    }
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    URLDriver driver = new URLDriver();
    driver.merge(url);
    return mount(driver);
  }
}
