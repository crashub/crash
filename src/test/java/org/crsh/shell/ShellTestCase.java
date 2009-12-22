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

import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.crsh.RepositoryBootstrap;
import org.crsh.util.IO;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.Session;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellTestCase extends TestCase {

  /** . */
  private Repository repo;

  /** . */
  private boolean initialized = false;

  /** . */
  private Shell shell;

  /** . */
  private GroovyShell groovyShell;

  /** . */
  private final ShellContext shellContext = new ShellContext() {
    public String loadScript(String resourceId) {
      // Remove leading '/'
      resourceId = resourceId.substring(1);
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId);
      return in != null ? IO.readAsUTF8(in) : null;
    }

    public ClassLoader getLoader() {
      return Thread.currentThread().getContextClassLoader();
    }
  };

  @Override
  protected void setUp() throws Exception {
    if (!initialized) {
      RepositoryBootstrap bootstrap = new RepositoryBootstrap();
      bootstrap.bootstrap();
      repo = bootstrap.getRepository();
      initialized = true;
    }

    //
    ShellBuilder builder = new ShellBuilder(shellContext);

    //
    shell = builder.build();
    groovyShell = shell.getGroovyShell();

    //
  }

  @Override
  protected void tearDown() throws Exception {
    if (shell != null) {
      shell.close();
    }
  }

  public void testAnonymousConnect() throws Exception {
    shell.evaluate("connect ws");
    assertNotNull(shell.getAttribute("session"));
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testRootConnect() throws Exception {
    shell.evaluate("connect -u root -p exo ws");
    assertNotNull(shell.getAttribute("session"));
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testCd() throws Exception {
    shell.evaluate("connect ws");
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    shell.evaluate("cd foo");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    shell.evaluate("cd ..");
    assertEquals("/", shell.getAttribute("currentPath"));
    shell.evaluate("cd /foo");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    shell.evaluate("cd .");
    assertEquals("/foo", shell.getAttribute("currentPath"));
    shell.evaluate("cd");
    assertEquals("/", shell.getAttribute("currentPath"));
  }

  public void testCommit() throws Exception {
    shell.evaluate("connect ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('added_node');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    shell.evaluate("commit");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('added_node')"));
  }

  public void testRollback() throws Exception {
    shell.evaluate("connect ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
    shell.evaluate("rollback");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testRm() throws Exception {
    shell.evaluate("connect ws");
    assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());

    //
    groovyShell.evaluate("session.rootNode.addNode('foo').addNode('bar');");
    shell.evaluate("rm foo/bar");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));

    //
    shell.evaluate("rm foo");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
  }

  public void testExportImport() throws Exception {
    shell.evaluate("connect ws");
    groovyShell.evaluate("session.rootNode.addNode('foo', 'nt:base');");
    shell.evaluate("exportnode /foo /foo.xml");

    //
    Node fooXML = (Node)groovyShell.evaluate("node = session.rootNode['foo.xml']");
    assertNotNull(fooXML);
    assertEquals("nt:file", fooXML.getPrimaryNodeType().getName());
    Node fooContent = fooXML.getNode("jcr:content");
    assertEquals("application/xml", fooContent.getProperty("jcr:mimeType").getString());

    //
    groovyShell.evaluate("session.rootNode.foo.remove()");
    shell.evaluate("importnode /foo.xml /");
    Node foo = (Node)groovyShell.evaluate("return session.rootNode.foo");
    assertNotNull(foo);
    assertEquals("foo", foo.getName());
  }

  public void testSet() throws Exception {
    shell.evaluate("connect ws");
    groovyShell.evaluate("session.rootNode.setProperty('foo_string', 'foo_value');");
    groovyShell.evaluate("session.rootNode.setProperty('foo_long', 3);");
    groovyShell.evaluate("session.rootNode.setProperty('foo_boolean', true);");

    // String update
    shell.evaluate("set /foo_string foo_value_2");
    assertEquals("foo_value_2", groovyShell.evaluate("return session.rootNode.getProperty('foo_string').string;"));

    // Long update
    shell.evaluate("set /foo_long 4");
    assertEquals(4L, groovyShell.evaluate("return session.rootNode.getProperty('foo_long').long;"));

    // Long update
    shell.evaluate("set /foo_boolean false");
    assertEquals(Boolean.FALSE, groovyShell.evaluate("return session.rootNode.getProperty('foo_boolean').boolean;"));

    // String create
    shell.evaluate("set /bar_string bar_value");
    assertEquals(PropertyType.STRING, groovyShell.evaluate("return session.rootNode.getProperty('bar_string').type;"));
    assertEquals("bar_value", groovyShell.evaluate("return session.rootNode.getProperty('bar_string').string;"));

    // Long create
    shell.evaluate("set -t LONG /bar_long 3");
    assertEquals(PropertyType.LONG, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').type;"));
    assertEquals(3L, groovyShell.evaluate("return session.rootNode.getProperty('bar_long').long;"));

    // Boolean create
    shell.evaluate("set -t BOOLEAN /bar_boolean true");
    assertEquals(PropertyType.BOOLEAN, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').type;"));
    assertEquals(true, groovyShell.evaluate("return session.rootNode.getProperty('bar_boolean').boolean;"));

    // Existing string remove
    shell.evaluate("set /foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));

    // Non existing string remove
    shell.evaluate("set /foo_string");
    assertEquals(false, groovyShell.evaluate("return session.rootNode.hasProperty('foo_string');"));

    // Missing unit test for node with existing meta data
  }
}
