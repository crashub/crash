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

public class CopyTestCase extends AbstractJCRCommandTestCase {

  /**
   * Copy a node in cwd to another
   * @throws Exception
   */
  public void testCopyRelativeToRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo bar");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('bar')"));
  }

  /**
   * Copy a node to an existing name
   */
  public void testCopyToExisting() {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo bar");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('bar[2]')"));
  }

  /**
   * Copy 2 relative paths
   * @throws Exception
   */
  public void testSubRelativeToSubRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('foo2');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo/foo2 foo/foo3");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('foo3')"));
  }

  /**
   * Copy an absolute path to another absolute path
   * @throws Exception
   */
  public void testAbsoluteToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp /foo/bar /zed");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('zed')"));
  }

  /**
   *  copy a relative source to an absolute destination
   * @throws Exception
   */
  public void testRelativeToSubRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));

  }

  /**
   * copy a relative source to an absolute destination
   * @throws Exception
   */
  public void testAbsoluteToRelative() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp /foo bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));

  }

  /**
   * copy a relative source to an absolute destination
   * @throws Exception
   */
  public void testRelativeToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo /bar/zed");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('bar').hasNode('zed')"));
  }

  /**
   * copy a relative source to an absolute destination
   * @throws Exception
   */
  public void testSubRelativeToAbsolute() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.getNode('foo').addNode('bar');");
    groovyShell.evaluate("session.save();");
    assertOk("cp foo/bar /zed");
    groovyShell.evaluate("session.refresh(true);");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('zed')"));
  }
}
