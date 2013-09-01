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

import javax.jcr.PropertyType;

public class SetTestCase extends AbstractJCRCommandTestCase {

  public void testSingleValued() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.setProperty('foo_string', 'foo_value');");
    groovyShell.evaluate("session.rootNode.setProperty('foo_long', 3);");
    groovyShell.evaluate("session.rootNode.setProperty('foo_boolean', true);");

    // String update
    assertOk("node set foo_string foo_value_2");
    assertEquals("foo_value_2", groovyShell.evaluate("return session.rootNode.getProperty('foo_string').string;"));

    // Long update
    assertOk("node set foo_long 4");
    assertEquals(4L, groovyShell.evaluate("return session.rootNode.getProperty('foo_long').long;"));

    // Long update
    assertOk("node set foo_boolean false");
    assertEquals(Boolean.FALSE, groovyShell.evaluate("return session.rootNode.getProperty('foo_boolean').boolean;"));

    // String create
    assertOk("node set bar_string bar_value");
    assertEquals(PropertyType.STRING, groovyShell.evaluate("return session.rootNode.getProperty('bar_string').type;"));
    assertEquals("bar_value", groovyShell.evaluate("return session.rootNode.getProperty('bar_string').string;"));

    // Long create
    assertOk("node set -t LONG bar_long 3");
    assertEquals(PropertyType.LONG, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').type;"));
    assertEquals(3L, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').long;"));

    // Boolean create
    assertOk("node set -t BOOLEAN bar_boolean true");
    assertEquals(PropertyType.BOOLEAN, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').type;"));
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').boolean;"));

    // Existing string remove
    assertOk("node set foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));

    // Non existing string remove
    assertOk("node set foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));
  }

  public void testMultiValued() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.setProperty('bar', ['1','2'] as String[])");
    assertOk("node set bar '3'");
    assertEquals(1, groovyShell.evaluate("return session.rootNode.getProperty('bar').values.length;"));
    assertEquals("3", groovyShell.evaluate("return session.rootNode.getProperty('bar').values[0].string;"));
  }

  public void testPipe() throws Exception {
    assertLogin();
    assertOk("produce / | node set foo_string foo_value");
    assertEquals("foo_value", groovyShell.evaluate("return session.rootNode.getProperty('foo_string').string;"));
  }
}
