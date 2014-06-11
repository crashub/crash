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
package org.crsh.ssh;

import junit.framework.TestCase;
import org.crsh.ssh.term.SSHContext;
import org.crsh.util.Utils;

/**
 * @author Julien Viet
 */
public class EncodingTestCase extends TestCase {

  public void testParseLC_CTYPE() {
    assertEquals(null, SSHContext.parseEncoding(""));
    assertEquals(null, SSHContext.parseEncoding("fr_FR"));
    assertEquals(null, SSHContext.parseEncoding("fr_FR@euro"));
    assertEquals(Utils.UTF_8, SSHContext.parseEncoding("UTF-8"));
    assertEquals(Utils.UTF_8, SSHContext.parseEncoding("fr_FR.UTF-8"));
    assertEquals(Utils.UTF_8, SSHContext.parseEncoding("fr_FR.UTF-8@euro"));
  }
}
