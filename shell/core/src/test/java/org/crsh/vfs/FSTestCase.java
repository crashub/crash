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

package org.crsh.vfs;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FSTestCase extends TestCase {
  public void testFoo() throws Exception {
    FS fs = new FS().mount(FSTestCase.class);
    File root = fs.get(Path.get("/"));
    File org = root.child("org", true);
    assertEquals("org", org.getName());
    assertEquals(true, org.isDir());
    Iterator<File> orgChildren = org.children().iterator();
    File crsh = orgChildren.next();
    assertFalse(orgChildren.hasNext());
    assertEquals("crsh", crsh.getName());
    assertEquals(true, crsh.isDir());
  }
}
