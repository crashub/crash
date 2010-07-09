/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.crsh.display.structure.Element;
import org.crsh.display.structure.LabelElement;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellTestCase extends AbstractCommandTestCase {

  public void testAnonymousConnect() throws Exception {
    assertOk("login ws");
    assertNotNull(shell.getAttribute("session"));
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testUnkownCommand() throws Exception {
    assertOk("login ws");
    assertUnknownCommand("bilto");
  }

  public void testRootConnect() throws Exception {
    assertOk("connect -u root -p exo ws");
    assertNotNull(shell.getAttribute("session"));
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testCd() throws Exception {
    assertOk("login ws");
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertOk("cd foo");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    assertOk("cd ..");
    assertEquals("/", shell.getAttribute("currentPath"));
    assertOk("cd /foo");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    assertOk("cd .");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    assertOk("cd");
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testCommit() throws Exception {
    assertOk("login ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('added_node');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertOk("commit");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('added_node')"));
  }

  public void testRollback() throws Exception {
    assertOk("login ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertOk("rollback");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testRm() throws Exception {
    assertOk("login ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());

    //
    groovyShell.evaluate("session.rootNode.addNode('foo').addNode('bar');");
    assertOk("rm foo/bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));

    //
    assertOk("rm foo");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testExportImport() throws Exception {
    assertOk("login ws");
    groovyShell.evaluate("session.rootNode.addNode('foo', 'nt:base');");
    assertOk("exportnode /foo /foo.xml");

    //
    Node fooXML = (Node)groovyShell.evaluate("node = session.rootNode['foo.xml']");
    assertNotNull(fooXML);
    assertEquals("nt:file", fooXML.getPrimaryNodeType().getName());
    Node fooContent = fooXML.getNode("jcr:content");
    assertEquals("application/xml", fooContent.getProperty("jcr:mimeType").getString());

    //
    groovyShell.evaluate("session.rootNode.foo.remove()");
    assertOk("importnode /foo.xml /");
    Node foo = (Node)groovyShell.evaluate("return session.rootNode.foo");
    assertNotNull(foo);
    assertEquals("foo", foo.getName());
  }
    
  public void testPWD() throws Exception {

    assertOk("login ws");
    ShellResponse resp = assertOk("pwd");
    Iterator<Element> elts = ((ShellResponse.Display)resp).iterator();
    assertTrue(elts.hasNext());
    assertEquals("/", ((LabelElement)elts.next()).getValue());
    groovyShell.evaluate("setCurrentNode(session.rootNode.addNode('foo'));");
    resp = assertOk("pwd");
    elts = ((ShellResponse.Display)resp).iterator();
    assertTrue(elts.hasNext());
    assertEquals("/foo", ((LabelElement)elts.next()).getValue());
  }

  public void testSet() throws Exception {
    assertOk("login ws");
    groovyShell.evaluate("session.rootNode.setProperty('foo_string', 'foo_value');");
    groovyShell.evaluate("session.rootNode.setProperty('foo_long', 3);");
    groovyShell.evaluate("session.rootNode.setProperty('foo_boolean', true);");

    // String update
    assertOk("set /foo_string foo_value_2");
    assertEquals("foo_value_2", groovyShell.evaluate("return session.rootNode.getProperty('foo_string').string;"));

    // Long update
    assertOk("set /foo_long 4");
    assertEquals(4L, groovyShell.evaluate("return session.rootNode.getProperty('foo_long').long;"));

    // Long update
    assertOk("set /foo_boolean false");
    assertEquals(Boolean.FALSE, groovyShell.evaluate("return session.rootNode.getProperty('foo_boolean').boolean;"));

    // String create
    assertOk("set /bar_string bar_value");
    assertEquals(PropertyType.STRING, groovyShell.evaluate("return session.rootNode.getProperty('bar_string').type;"));
    assertEquals("bar_value", groovyShell.evaluate("return session.rootNode.getProperty('bar_string').string;"));

    // Long create
    assertOk("set -t LONG /bar_long 3");
    assertEquals(PropertyType.LONG, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').type;"));
    assertEquals(3L, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').long;"));

    // Boolean create
    assertOk("set -t BOOLEAN /bar_boolean true");
    assertEquals(PropertyType.BOOLEAN, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').type;"));
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').boolean;"));

    // Existing string remove
    assertOk("set /foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));

    // Non existing string remove
    assertOk("set /foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));

    // Missing unit test for node with existing meta data
  }
}
