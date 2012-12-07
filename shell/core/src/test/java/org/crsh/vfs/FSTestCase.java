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

import junit.framework.TestCase;
import org.crsh.util.IO;
import org.crsh.vfs.spi.ram.RAMDriver;
import org.crsh.vfs.spi.url.Node;
import org.crsh.vfs.spi.url.URLDriver;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

public class FSTestCase extends TestCase {
  public void testFoo() throws Exception {
    FS fs = new FS().mount(FSTestCase.class);
    File root = fs.get(Path.get("/"));
    File org = root.child("org", true);
    assertEquals("org", org.getName());
    assertEquals(true, org.isDir());
    Iterator<File> orgChildren = org.children().iterator();
    File crsh = orgChildren.next();
    assertFalse(orgChildren.hasNext());
    assertEquals("crsh", crsh.getName());
    assertEquals(true, crsh.isDir());
  }

  public void testJar() throws Exception {
    // Generate test jar
    java.io.File file = java.io.File.createTempFile("test", ".jar");
    file.deleteOnExit();
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
    archive.addClass(FSTestCase.class);
    ZipExporter exporter = archive.as(ZipExporter.class);
    exporter.exportTo(file, true);

    //
    URLDriver driver = new URLDriver();
    driver.merge(file.toURI().toURL());
    Node root = driver.root();
    assertEquals("", driver.name(root));
    assertTrue(driver.isDir(root));

    //
    Iterator<Node> rootChildren = driver.children(root).iterator();
    Node org = rootChildren.next();
    assertFalse(rootChildren.hasNext());
    assertEquals("org", driver.name(org));
    assertTrue(driver.isDir(org));

    //
    Iterator<Node> orgChildren = driver.children(org).iterator();
    Node crsh = orgChildren.next();
    assertFalse(orgChildren.hasNext());
    assertEquals("crsh", driver.name(crsh));
    assertTrue(driver.isDir(crsh));

    //
    Iterator<Node> vfsChildren = driver.children(crsh).iterator();
    Node vfs = vfsChildren.next();
    assertFalse(vfsChildren.hasNext());
    assertEquals("vfs", driver.name(vfs));
    assertTrue(driver.isDir(vfs));

    //
    Iterator<Node> clazzChildren = driver.children(vfs).iterator();
    Node clazz = clazzChildren.next();
    assertFalse(clazzChildren.hasNext());
    assertEquals(FSTestCase.class.getSimpleName() + ".class", driver.name(clazz));
    assertFalse(driver.isDir(clazz));
    InputStream in = driver.open(clazz);
    in.close();
  }

  public void testNestedJar() throws Exception {
    java.io.File file = java.io.File.createTempFile("test", ".war");
    file.deleteOnExit();
    JavaArchive jar = ShrinkWrap.create(JavaArchive.class,"foo.jar");
    jar.addClass(FSTestCase.class);
    WebArchive war = ShrinkWrap.create(WebArchive.class);
    war.addAsLibraries(jar);
    ZipExporter exporter = war.as(ZipExporter.class);
    exporter.exportTo(file, true);

    //
    URL url = new URL("jar:" + file.toURI().toURL() + "!/WEB-INF/lib/foo.jar");
    URLDriver driver = new URLDriver();
    driver.merge(new URL("jar:" + file.toURI().toURL() + "!/WEB-INF/"));
    Node root = driver.root();
    Node lib = driver.child(root, "lib");
    Node foo_jar = driver.child(lib, "foo.jar");
    assertNotNull(foo_jar);
    InputStream in = driver.open(foo_jar);
    assertNotNull(in);
    byte[] bytes = IO.readAsBytes(in);

    //
    url = new URL("jar:" + url + "!/org/crsh/");
    driver = new URLDriver();
    driver.merge(url);
    root = driver.root();
    Node vfs = driver.child(root, "vfs");
    Node FSTestCase_class = driver.child(vfs, "FSTestCase.class");
    assertNotNull(FSTestCase_class);
    in = driver.open(FSTestCase_class);
    assertNotNull(in);
    bytes = IO.readAsBytes(in);
  }

  public void testRAM() throws Exception {
    RAMDriver driver = new RAMDriver();
    driver.add("/foo", "bar");
    Path root = driver.root();
    assertEquals(Path.get("/"), root);
    Path foo = driver.child(root, "foo");
    assertNotNull(foo);
    InputStream in = driver.open(foo);
    String file = IO.readAsUTF8(in);
    assertEquals("bar", file);
  }
}
