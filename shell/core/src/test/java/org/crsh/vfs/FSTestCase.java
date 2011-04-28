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

package org.crsh.vfs;

import junit.framework.TestCase;
import org.crsh.util.IO;
import org.crsh.vfs.spi.jarurl.*;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
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
    OutputStream out = new FileOutputStream(file);
    JarOutputStream jos = new JarOutputStream(out);
    String name = FSTestCase.class.getName().replace('.', '/') + ".class";
    JarEntry entry = new JarEntry(name);
    int prev = 0;
    while (true) {
      int pos = name.indexOf('/', prev);
      if (pos == -1) {
        break;
      } else {
        prev = pos + 1;
        jos.putNextEntry(new JarEntry(name.substring(0, prev)));
        jos.closeEntry();
      }
    }
    jos.putNextEntry(entry);
    InputStream classStream = FSTestCase.class.getClassLoader().getResourceAsStream(name);
    IO.copy(classStream, jos);
    jos.closeEntry();
    jos.close();

    //
    URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()});
    URL classURL = cl.findResource(FSTestCase.class.getName().replace('.', '/') + ".class");
    JarURLConnection conn = (JarURLConnection)classURL.openConnection();

    //
    JarURLDriver driver = new JarURLDriver(conn);
    org.crsh.vfs.spi.jarurl.Handle root = driver.root();
    assertEquals("", driver.name(root));
    assertTrue(driver.isDir(root));

    //
    Iterator<org.crsh.vfs.spi.jarurl.Handle> rootChildren = driver.children(root).iterator();
    org.crsh.vfs.spi.jarurl.Handle org = rootChildren.next();
    assertFalse(rootChildren.hasNext());
    assertEquals("org", driver.name(org));
    assertTrue(driver.isDir(org));

    //
    Iterator<org.crsh.vfs.spi.jarurl.Handle> orgChildren = driver.children(org).iterator();
    org.crsh.vfs.spi.jarurl.Handle crsh = orgChildren.next();
    assertFalse(orgChildren.hasNext());
    assertEquals("crsh", driver.name(crsh));
    assertTrue(driver.isDir(crsh));

    //
    Iterator<org.crsh.vfs.spi.jarurl.Handle> vfsChildren = driver.children(crsh).iterator();
    org.crsh.vfs.spi.jarurl.Handle vfs = vfsChildren.next();
    assertFalse(vfsChildren.hasNext());
    assertEquals("vfs", driver.name(vfs));
    assertTrue(driver.isDir(vfs));

    //
    Iterator<org.crsh.vfs.spi.jarurl.Handle> clazzChildren = driver.children(vfs).iterator();
    org.crsh.vfs.spi.jarurl.Handle clazz = clazzChildren.next();
    assertFalse(clazzChildren.hasNext());
    assertEquals(FSTestCase.class.getSimpleName() + ".class", driver.name(clazz));
    assertFalse(driver.isDir(clazz));
    URL clazzURL = driver.toURL(clazz);
    InputStream in = clazzURL.openStream();
    in.close();
  }
}
