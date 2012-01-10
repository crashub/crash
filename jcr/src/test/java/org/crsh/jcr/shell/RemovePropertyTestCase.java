/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.crsh.jcr.shell;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RemovePropertyTestCase extends AbstractJCRCommandTestCase {

  @Override
  public void setUp() throws Exception {

    super.setUp();
    assertLogin();

  }

  @Override
  public void tearDown() throws Exception {

    groovyShell.evaluate("session.rootNode.getNode('foo').remove();");
    super.tearDown();

  }

  public void testRemoveSingleExisting() throws Exception {

    groovyShell.evaluate("session.rootNode.addNode('foo').setProperty('bar', 'foobar');");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));
    assertOk("cd foo");
    assertOk("node set bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));

  }

  public void testRemoveMultipleExisting() throws Exception {

    groovyShell.evaluate("session.rootNode.addNode('foo').setProperty('bar', ['foobar1', 'foobar2'].toArray(new String()[]));");
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));
    assertOk("cd foo");
    assertOk("node set bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));

  }

  public void testRemoveMissing() throws Exception {

    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));
    assertOk("cd foo");
    assertOk("node set bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasProperty('bar')"));

  }

}
