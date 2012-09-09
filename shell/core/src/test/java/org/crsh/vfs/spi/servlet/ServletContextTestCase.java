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

package org.crsh.vfs.spi.servlet;

import junit.framework.TestCase;

import java.util.regex.Matcher;

public class ServletContextTestCase extends TestCase {

  private void assertMatch(String s, String name, String trail) {
    Matcher m = ServletContextDriver.pathPattern.matcher(s);
    assertTrue(m.matches());
    assertEquals(name, m.group(1));
    assertEquals(trail, m.group(2));
  }

  private void assertNotMatch(String s) {
    Matcher m = ServletContextDriver.pathPattern.matcher(s);
    assertFalse(m.matches());
  }

  public void testMatch() throws Exception {
    assertMatch("/", null, "/");
    assertMatch("/a", "a", "");
    assertMatch("/a/", "a", "/");
    assertMatch("/a/b", "b", "");
    assertMatch("/a/b/", "b", "/");
  }

  public void testNotMatch() throws Exception {
    assertNotMatch("a");
    assertNotMatch("a/");
    assertNotMatch("a/a");
  }
}
