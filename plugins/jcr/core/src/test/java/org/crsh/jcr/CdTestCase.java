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

import org.crsh.jcr.command.Path;

public class CdTestCase extends AbstractJCRCommandTestCase {



  public void testCd() throws Exception {
    assertLogin();
    groovyShell.evaluate("session.rootNode.addNode('foo');");
    groovyShell.evaluate("session.rootNode.addNode('foo bar');");

    //
    assertOk("cd foo");
    assertEquals(new Path("/foo"), shell.get("currentPath"));
    assertOk("cd ..");
    assertEquals(new Path("/"), shell.get("currentPath"));
    assertOk("cd /foo");
    assertEquals(new Path("/foo"), shell.get("currentPath"));
    assertOk("cd .");
    assertEquals(new Path("/foo"), shell.get("currentPath"));
    assertOk("cd");
    assertEquals(new Path("/"), shell.get("currentPath"));

    //
    assertOk("cd 'foo bar'");
    assertEquals(new Path("/foo bar"), shell.get("currentPath"));
    assertOk("cd ..");
    assertEquals(new Path("/"), shell.get("currentPath"));
    assertOk("cd '/foo bar'");
    assertEquals(new Path("/foo bar"), shell.get("currentPath"));
    assertOk("cd .");
    assertEquals(new Path("/foo bar"), shell.get("currentPath"));
    assertOk("cd");
    assertEquals(new Path("/"), shell.get("currentPath"));
  }

}
