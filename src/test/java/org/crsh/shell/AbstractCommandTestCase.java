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
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractCommandTestCase extends TestCase {

  /** . */
  protected Repository repo;

  /** . */
  private boolean initialized = false;

  /** . */
  protected Shell shell;

  /** . */
  protected GroovyShell groovyShell;

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
    cleanRoot();
  }

  @Override
  protected void tearDown() throws Exception {
    if (shell != null) {
      shell.close();
      shell = null;
      groovyShell = null;
    }
  }

  private void cleanRoot() throws Exception {
    shell.evaluate("connect ws");
    Node root = (Node)groovyShell.evaluate("session.rootNode");
    root.refresh(false);
    NodeIterator it = root.getNodes();
    while (it.hasNext())
    {
      Node n = it.nextNode();
      if(!n.getName().equals("jcr:system")) {
        System.out.println("removed " + n.getPath());
        n.remove();
      }
    }
    root.getSession().save();
    shell.evaluate("disconnect");
  }
}