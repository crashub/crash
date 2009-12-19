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
import org.crsh.shell.Shell;
import org.crsh.shell.ShellBuilder;
import org.crsh.shell.ShellContext;
import org.crsh.util.IO;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellTestCase extends TestCase
{

   /** . */
   private Repository repo;

   /** . */
   private boolean initialized = false;

   /** . */
   private Shell shell;

   /** . */
   private GroovyShell groovyShell;

   /** . */
   private final ShellContext shellContext = new ShellContext()
   {
      public String loadScript(String resourceId)
      {
         // Remove leading '/'
         resourceId = resourceId.substring(1);
         InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId);
         return in != null ? IO.readAsUTF8(in) : null;
      }
      public ClassLoader getLoader()
      {
         return Thread.currentThread().getContextClassLoader();
      }
   };

   @Override
   protected void setUp() throws Exception
   {
      if (!initialized)
      {
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
   protected void tearDown() throws Exception
   {
      if (shell != null)
      {
         shell.close();
      }
   }

   public void testAnonymousConnect() throws Exception
   {
      shell.evaluate2("connect ws");
      assertNotNull(shell.getAttribute("session"));
      assertEquals("/", shell.getAttribute("currentPath"));
   }

   public void testRootConnect() throws Exception
   {
      shell.evaluate2("connect ws root exo");
      assertNotNull(shell.getAttribute("session"));
      assertEquals("/", shell.getAttribute("currentPath"));
   }

   public void testCd() throws Exception
   {
      shell.evaluate2("connect ws");
      groovyShell.evaluate("session.rootNode.addNode('foo');");
      shell.evaluate2("cd foo");
      assertEquals("/foo", shell.getAttribute("currentPath"));
      shell.evaluate2("cd ..");
      assertEquals("/", shell.getAttribute("currentPath"));
      shell.evaluate2("cd /foo");
      assertEquals("/foo", shell.getAttribute("currentPath"));
      shell.evaluate2("cd .");
      assertEquals("/foo", shell.getAttribute("currentPath"));
      shell.evaluate2("cd");
      assertEquals("/", shell.getAttribute("currentPath"));
   }

   public void testCommit() throws Exception
   {
      shell.evaluate2("connect ws");
      assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
      groovyShell.evaluate("session.rootNode.addNode('added_node');");
      assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
      shell.evaluate2("commit");
      assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
      assertEquals(true, groovyShell.evaluate("return session.rootNode.hasNode('added_node')"));
   }

   public void testRollback() throws Exception
   {
      shell.evaluate2("connect ws");
      assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
      groovyShell.evaluate("session.rootNode.addNode('foo');");
      assertTrue(((Session)shell.getAttribute("session")).hasPendingChanges());
      shell.evaluate2("rollback");
      assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());
      assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
   }

   public void testRm() throws Exception
   {
      shell.evaluate2("connect ws");
      assertFalse(((Session)shell.getAttribute("session")).hasPendingChanges());

      //
      groovyShell.evaluate("session.rootNode.addNode('foo').addNode('bar');");
      shell.evaluate2("rm foo/bar");
      assertEquals(false, groovyShell.evaluate("return session.rootNode.getNode('foo').hasNode('bar')"));

      //
      shell.evaluate2("rm foo");
      assertEquals(false, groovyShell.evaluate("return session.rootNode.hasNode('foo')"));
   }

   public void testExport() throws Exception
   {
      shell.evaluate2("connect ws");
      groovyShell.evaluate("session.rootNode.addNode('foo', 'nt:base');");
      shell.evaluate2("export /foo /foo.xml");
      Node fooXML = (Node)groovyShell.evaluate("node = session.rootNode['foo.xml']");
      assertNotNull(fooXML);
      assertEquals("nt:file", fooXML.getPrimaryNodeType().getName());
      Node fooContent = fooXML.getNode("jcr:content");
      assertEquals("application/xml", fooContent.getProperty("jcr:mimeType").getString());
   }
}
