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
package org.crsh.shell.jcr;

import org.crsh.command.ScriptException;
import org.crsh.shell.AbstractCommandTestCase;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellTestCase extends AbstractCommandTestCase {

  public void testCommit() throws Exception {
    assertLogin();
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('added_node');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertOk("commit");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('added_node')"));
  }

  public void testRollback() throws Exception {
    assertLogin();
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertOk("rollback");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testExportImport() throws Exception {
    assertLogin();
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
    assertOk("addnode foo | addnode bar");
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
    Iterator<?> produced = assertOk("produce / | set foo foo_value + set bar bar_value + consume").getProduced().iterator();
    assertTrue(produced.hasNext());
    assertEquals("/", ((Node)produced.next()).getPath());
    assertFalse(produced.hasNext());
    assertEquals("foo_value", groovyShell.evaluate("return session.rootNode.getProperty('foo').string;"));
    assertEquals("bar_value", groovyShell.evaluate("return session.rootNode.getProperty('bar').string;"));
  }

  public void testAggregateContent() throws Exception {
    assertOk("foobar", "echo foo + echo bar");
  }

  public void testKeepLastPipeContent() throws Exception {
    assertOk("bar", "echo foo | echo bar");
  }
}
