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

import org.crsh.shell.ShellResponse;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.Iterator;

public class ShellTestCase extends AbstractJCRCommandTestCase {

  public void testCommit() throws Exception {
    assertLogin();
    assertFalse(((Session)shell.get("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('added_node');");
    assertTrue(((Session)shell.get("session")).hasPendingChanges());
    assertOk("commit");
    assertFalse(((Session)shell.get("session")).hasPendingChanges());
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('added_node')"));
  }

  public void testRollback() throws Exception {
    assertLogin();
    assertFalse(((Session)shell.get("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertTrue(((Session)shell.get("session")).hasPendingChanges());
    assertOk("rollback");
    assertFalse(((Session)shell.get("session")).hasPendingChanges());
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testExportImport() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo', 'nt:folder');");
    assertOk("node export /foo /foo.xml");

    //
    Node fooXML = (Node)groovyShell.evaluate("node = session.rootNode['foo.xml']");
    assertNotNull(fooXML);
    assertEquals("nt:file", fooXML.getPrimaryNodeType().getName());
    Node fooContent = fooXML.getNode("jcr:content");
    assertEquals("application/xml", fooContent.getProperty("jcr:mimeType").getString());

    //
    groovyShell.evaluate("session.rootNode.foo.remove()");
    assertOk("node import /foo.xml /");
    Node foo = (Node)groovyShell.evaluate("return session.rootNode.foo");
    assertNotNull(foo);
    assertEquals("foo", foo.getName());
  }
    
  public void testPWD() throws Exception {
/*
    assertLogin();
    ShellResponse resp = assertOk("pwd");
    Iterator<Element> elts = ((ShellResponse.Display)resp).iterator();
    assertTrue(elts.hasNext());
    assertEquals("/", ((LabelElement)elts.next()).getValue());
    groovyShell.evaluate("setCurrentNode(session.rootNode.addNode('foo'));");
    resp = assertOk("pwd");
    elts = ((ShellResponse.Display)resp).iterator();
    assertTrue(elts.hasNext());
    assertEquals("/foo", ((LabelElement)elts.next()).getValue());
*/
  }

  public void testPipe() throws Exception {
    assertLogin();
    assertOk("node add foo | node add bar");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('bar');"));
  }

  public void testPipe2() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo').setProperty('bar','juu');");
    groovyShell.evaluate("session.save();");
    assertOk("select * from nt:base where bar = 'juu' | rm");
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo');"));
  }

  public void testDistribution() throws Exception {
    assertLogin();
    String produced = assertOk("produce / | node set foo foo_value | node set bar bar_value | consume");
    assertEquals("/\n", produced);
    assertEquals("foo_value", groovyShell.evaluate("return session.rootNode.getProperty('foo').string;"));
    assertEquals("bar_value", groovyShell.evaluate("return session.rootNode.getProperty('bar').string;"));
  }
}
