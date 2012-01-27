/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.jcr.shell;

import org.crsh.shell.AbstractCommandTestCase;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractJCRCommandTestCase extends AbstractCommandTestCase {

  public AbstractJCRCommandTestCase() {
  }

  public AbstractJCRCommandTestCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {

    // Ensure everything is fine
    GroovyRepositoryBootstrap.getRepository();

    //
    super.setUp();

    //
    cleanRoot();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
  }

  private void cleanRoot() throws Exception {
    assertLogin();
    Node root = (Node)groovyShell.evaluate("session.rootNode");
    root.refresh(false);
    NodeIterator it = root.getNodes();
    while (it.hasNext()) {
      Node n = it.nextNode();
      if(!n.getName().equals("jcr:system")) {
        log.debug("Removed node " + n.getPath());
        n.remove();
      }
    }
    root.getSession().save();
    evaluate("ws logout");
  }

  protected final void assertLogin() {
    assertOk("ws login -u exo -p exo ws");
  }

  protected final List<String> getStringValues(Property p) throws RepositoryException {
    List<String> strings = new ArrayList<String>();
    for (Value value : p.getValues()) {
      strings.add(value.getString());
    }
    return strings;
  }
}
