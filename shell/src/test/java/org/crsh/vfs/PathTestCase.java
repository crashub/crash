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

import java.util.Iterator;

public class PathTestCase extends TestCase {

  public void testPath() throws Exception {
    assertSamePath("", false, true);
    assertSamePath("/", true, true);
    assertSamePath("//", true, true);
    assertSamePath("/a", true, false, "a");
    assertSamePath("//a", true, false, "a");
    assertSamePath("a", false, false, "a");
    assertSamePath("/a/", true, true, "a");
    assertSamePath("//a/", true, true, "a");
    assertSamePath("a/", false, true, "a");
    assertSamePath("/a/b", true, false, "a", "b");
    assertSamePath("/a//b", true, false, "a", "b");
    assertSamePath("//a/b", true, false, "a", "b");
    assertSamePath("a/b", false, false, "a", "b");
    assertSamePath("a//b", false, false, "a", "b");
    assertSamePath("/a/b/", true, true, "a", "b");
    assertSamePath("/a//b/", true, true, "a", "b");
    assertSamePath("//a/b/", true, true, "a", "b");
    assertSamePath("//a/b//", true, true, "a", "b");
    assertSamePath("a/b/", false, true, "a", "b");
    assertSamePath("a//b/", false, true, "a", "b");
    assertSamePath("a/b//", false, true, "a", "b");
  }

  private void assertSamePath(String s, boolean absolute, boolean dir, String... expectedNames) {
    Path path = Path.get(s);
    assertEquals(dir, path.isDir());
    assertEquals(absolute, path.isAbsolute());
    assertEquals(path.getSize(), expectedNames.length);
    for (int index = 0;index < expectedNames.length;index++) {
      assertEquals(expectedNames[index], path.nameAt(index));
    }
  }
}
