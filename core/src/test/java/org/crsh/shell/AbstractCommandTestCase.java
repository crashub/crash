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
import junit.framework.AssertionFailedError;
import org.crsh.AbstractRepositoryTestCase;
import org.crsh.TestShellContext;
import org.crsh.shell.impl.CRaSH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractCommandTestCase extends AbstractRepositoryTestCase {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  protected CRaSH shell;

  /** . */
  protected GroovyShell groovyShell;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //
    ShellFactory builder = new ShellFactory(new TestShellContext());

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

  protected final List<String> getStringValues(Property p) throws RepositoryException {
    List<String> strings = new ArrayList<String>();
    for (Value value : p.getValues()) {
      strings.add(value.getString());
    }
    return strings;
  }

  protected final void assertUnknownCommand(String s) {
    ShellResponse resp = shell.evaluate(s);
    assertTrue("Was expecting an ok response instead of " + resp, resp instanceof ShellResponse.UnkownCommand);
    assertEquals(s, ((ShellResponse.UnkownCommand)resp).getName());
  }

  protected final Throwable assertError(String s) {
    ShellResponse resp = shell.evaluate(s);
    assertTrue("Was expecting an ok response instead of " + resp, resp instanceof ShellResponse.Error);
    return ((ShellResponse.Error)resp).getThrowable();
  }

  protected final ShellResponse.Ok assertOk(String s) {
    ShellResponse resp = shell.evaluate(s);
    if (resp instanceof ShellResponse.Ok)
    {
      return (ShellResponse.Ok)resp;
    }
    else if (resp instanceof ShellResponse.Error)
    {
      ShellResponse.Error err = (ShellResponse.Error)resp;
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(err.getThrowable());
      throw afe;
    }
    else
    {
      throw new AssertionFailedError("Was expecting an ok response instead of " + resp);
    }
  }

  private void cleanRoot() throws Exception {
    shell.evaluate("login ws");
    Node root = (Node)groovyShell.evaluate("session.rootNode");
    root.refresh(false);
    NodeIterator it = root.getNodes();
    while (it.hasNext())
    {
      Node n = it.nextNode();
      if(!n.getName().equals("jcr:system")) {
        log.debug("Removed node " + n.getPath());
        n.remove();
      }
    }
    root.getSession().save();
    shell.evaluate("disconnect");
  }
}