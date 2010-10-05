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

package org.crsh.shell;

import javax.jcr.Node;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SetPermTestCase extends AbstractCommandTestCase {

  public void testAddPrivilegeableMixin() throws Exception {
    assertOk("login ws");
    Node foo = (Node) groovyShell.evaluate("return session.rootNode.addNode('foo');");
    assertOk("setperm -n /foo julien");
    assertTrue(foo.isNodeType("exo:privilegeable"));
  }

  public void testSimplePermission() throws Exception {
    assertOk("login ws");
    Node foo = (Node) groovyShell.evaluate("return session.rootNode.addNode('foo');");

    // Test add
    assertOk("setperm -n /foo julien read");
    assertTrue(getStringValues(foo.getProperty("exo:permissions")).contains("julien read"));

    // Test remove
    assertOk("setperm -n /foo julien !read");
    assertFalse(getStringValues(foo.getProperty("exo:permissions")).contains("julien read"));
  }

  public void testMultiplePermission() throws Exception {
    assertOk("login ws");
    Node foo = (Node) groovyShell.evaluate("return session.rootNode.addNode('foo');");

    // Test add
    assertOk("setperm -n /foo julien read add_node");
    assertTrue(getStringValues(foo.getProperty("exo:permissions")).contains("julien read"));
    assertTrue(getStringValues(foo.getProperty("exo:permissions")).contains("julien add_node"));
  }

  public void testCurrentNode() throws Exception {
    assertOk("login ws");
    Node foo = (Node) groovyShell.evaluate("return session.rootNode.addNode('foo');");
    assertOk("cd foo");

    // Test add
    assertOk("setperm julien read");
    assertTrue(getStringValues(foo.getProperty("exo:permissions")).contains("julien read"));
  }
}
