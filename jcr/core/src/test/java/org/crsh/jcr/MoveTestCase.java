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
import java.util.Iterator;

public class MoveTestCase extends AbstractJCRCommandTestCase {

  /**
   * Move a node in cwd to new
   * @throws Exception any exception
   */
  public void testRelativeToRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo bar");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('bar')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }


  /**
   * Move a node to an existing name
   */
  public void testMoveToExisting() {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo bar");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('bar[2]')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  /**
   * move 2 relative paths
   * @throws Exception any exception
   */
  public void testSubRelativeToSubRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo/bar foo/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('zed')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));
  }

  /**
   * Move an absolute path to another absolute path
   * @throws Exception any exception
   */
  public void testAbsoluteToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    String produced = assertOk("mv /foo/bar /zed | consume");
    assertEquals("/zed\n", produced);
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('zed')"));
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));
  }

  /**
   * Move a relative source to an absolute destination
   * @throws Exception any exception
   */
  public void testRelativeToSubRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  /**
   * Move a relative source to an absolute destination
   * @throws Exception any exception
   */
  public void testAbsoluteToRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv /foo bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  /**
   * Move a relative source to an absolute destination
   * @throws Exception any exception
   */
  public void testRelativeToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo /bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  /**
   * Move a relative source to an absolute destination
   * @throws Exception any exception
   */
  public void testSubRelativeToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("mv foo/bar /zed");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('zed')"));
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));
  }

  public void testConsume() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.rootNode.addNode('juu');");
    groovyShell.evaluate("session.save();");
    String produced = assertOk("produce foo bar | mv juu | consume");
    groovyShell.evaluate("session.refresh(true);");

//    assertEquals("/foo\n/bar\n", produced);

    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('juu/foo')"));
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.hasNode('juu/bar')"));
  }

  public void testNonTrivialRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cd foo ");
    assertOk("mv bar juu");
    groovyShell.evaluate("session.refresh(true);");
    assertTrue((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('juu')"));
    assertFalse((Boolean)groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));
  }
}
