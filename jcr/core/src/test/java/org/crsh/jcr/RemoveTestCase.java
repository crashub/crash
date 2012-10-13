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

import org.crsh.command.ScriptException;

import javax.jcr.Session;

public class RemoveTestCase extends AbstractJCRCommandTestCase {

  public void testRm() throws Exception {
    assertLogin();
    assertFalse(((Session)shell.get("session")).hasPendingChanges());

    // Try relative
    groovyShell.evaluate("session.rootNode.addNode('foo').addNode('bar');");
    assertOk("rm foo/bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));

    // Try absolute
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    assertOk("rm /foo/bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));

    // Try several
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    assertOk("rm foo bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('bar')"));

    // Delete a non existing node
    assertEvalError("rm foo", ScriptException.class);
  }

  public void testConsume() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    assertOk("produce /foo /bar | rm");
    assertFalse((Boolean)groovyShell.evaluate("session.rootNode.hasNode('foo');"));
    assertFalse((Boolean)groovyShell.evaluate("session.rootNode.hasNode('bar');"));
  }
}
