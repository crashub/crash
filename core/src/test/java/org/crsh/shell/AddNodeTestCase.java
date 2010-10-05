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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AddNodeTestCase extends AbstractCommandTestCase {

  public void testAddRelativeNode() throws Exception {
    assertOk("login ws");
    assertOk("addnode foo");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
    assertOk("cd foo");
    assertOk("addnode ../bar");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('bar');"));
  }

  public void testAddAbsoluteNode() throws Exception {
    assertOk("login ws");
    assertOk("addnode /foo");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
    assertOk("cd foo");
    assertOk("addnode /bar");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('bar');"));
  }

  public void testAddNodes() throws Exception {
    assertOk("login ws");
    assertOk("addnode foo /foo/bar");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo/bar');"));
  }

  public void testAddWithNodeType() throws Exception {
    assertOk("login ws");
    assertOk("addnode -t nt:file foo");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
    assertEquals("nt:file", groovyShell.evaluate("return session.rootNode.getNode('foo').primaryNodeType.name;"));
  }
}
