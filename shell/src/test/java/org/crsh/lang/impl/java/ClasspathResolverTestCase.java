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
package org.crsh.lang.impl.java;

import org.crsh.AbstractTestCase;
import org.crsh.util.Utils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** @author Julien Viet */
public class ClasspathResolverTestCase extends AbstractTestCase {

  final ClassLoader parent = new ClassLoader() {
    @Override
    public URL getResource(String name) {
      return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      return Collections.enumeration(Collections.<URL>emptyList());
    }
  };

  /** . */
  private JavaArchive archive;
  
  private static List<JavaFileObject> collect(List<JavaFileObject> files) {
    TreeMap<String, JavaFileObject> map = new TreeMap<String, JavaFileObject>();
    for (JavaFileObject file : files) {
      String name = file.getName();
      int index = name.lastIndexOf('/');
      String key = name.substring(Math.max(0, index));
      map.put(key, file);
    }
    return new ArrayList<JavaFileObject>(map.values());
  }

  @Override
  protected void setUp() throws Exception {
    URL manifest = Thread.currentThread().getContextClassLoader().getResource("META-INF/MANIFEST.MF");
    archive = ShrinkWrap.create(JavaArchive.class, "my.jar");
    archive.addClass(HashMap.class);
    archive.addClass(Map.class);
    archive.addClass(ConcurrentHashMap.class);
    archive.setManifest(manifest);
  }

  public void testDir() throws Exception {
    File root = toExploded(archive, "");
    ClassLoader cl = new URLClassLoader(new URL[]{root.toURI().toURL()}, parent);
    ClasspathResolver resolver = new ClasspathResolver(cl);

    // No recurse
    List<JavaFileObject> classes = collect(Utils.list(resolver.resolve("java.util", false)));
    assertEquals(2, classes.size());
    assertEndsWith("/HashMap.class", classes.get(0).getName());
    assertEndsWith("/Map.class", classes.get(1).getName());

    // Recurse
    classes = collect(Utils.list(resolver.resolve("java.util", true)));
    assertEquals(3, classes.size());
    assertEndsWith("/ConcurrentHashMap.class", classes.get(0).getName());
    assertEndsWith("/HashMap.class", classes.get(1).getName());
    assertEndsWith("/Map.class", classes.get(2).getName());
  }


  public void testJar() throws Exception {
    File jar = toFile(this.archive, ".jar");
    ClassLoader cl = new URLClassLoader(new URL[]{jar.toURI().toURL()}, parent);
    ClasspathResolver resolver = new ClasspathResolver(cl);

    // No recurse
    List<JavaFileObject> classes = collect(Utils.list(resolver.resolve("java.util", false)));
    assertEquals(2, classes.size());
    assertEndsWith("/HashMap.class", classes.get(0).getName());
    assertEndsWith("/Map.class", classes.get(1).getName());

    // Recurse
    classes = collect(Utils.list(resolver.resolve("java.util", true)));
    assertEquals(3, classes.size());
    assertEndsWith("/ConcurrentHashMap.class", classes.get(0).getName());
    assertEndsWith("/HashMap.class", classes.get(1).getName());
    assertEndsWith("/Map.class", classes.get(2).getName());
  }

  public void testNestedJar() throws Exception {
    final File war = toFile(ShrinkWrap.create(WebArchive.class).addAsLibrary(archive), ".jar");
    ClassLoader cl = new ClassLoader(parent) {
      @Override
      protected Enumeration<URL> findResources(String name) throws IOException {
        if ("META-INF/MANIFEST.MF".equals(name)) {
          URL u1 = new URL("jar:" + war.toURI().toURL() + "!/META-INF/MANIFEST.MF");
          URL u2 = new URL("jar:" + ("jar:" + war.toURI().toURL() + "!/WEB-INF/lib/my.jar") + "!/META-INF/MANIFEST.MF");
          return Collections.enumeration(Arrays.asList(u1, u2));
        } else if ("java/util".equals(name)) {
          String u = "jar:" + ("jar:" + war.toURI().toURL() + "!/WEB-INF/lib/my.jar") + "!/java/util/";
          return Collections.enumeration(Collections.singleton(new URL(u)));
        } else {
          return super.findResources(name);
        }
      }
    };
    ClasspathResolver resolver = new ClasspathResolver(cl);

    // No recurse
    List<JavaFileObject> classes = collect(Utils.list(resolver.resolve("java.util", false)));
    assertEquals(2, classes.size());
    assertEndsWith("/HashMap.class", classes.get(0).getName());
    assertEndsWith("/Map.class", classes.get(1).getName());

    // Attempt to get stream
    InputStream in = classes.get(0).openInputStream();
    byte[] bytes = Utils.readAsBytes(in);
    assertTrue(bytes.length > 0);

    // Recurse
    classes = collect(Utils.list(resolver.resolve("java.util", true)));
    assertEquals(3, classes.size());
    assertEndsWith("/ConcurrentHashMap.class", classes.get(0).getName());
    assertEndsWith("/HashMap.class", classes.get(1).getName());
    assertEndsWith("/Map.class", classes.get(2).getName());
  }
}
