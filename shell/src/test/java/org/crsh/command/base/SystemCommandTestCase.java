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

package org.crsh.command.base;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.AbstractShellTestCase;

public class SystemCommandTestCase extends AbstractShellTestCase {

  public void testFoo() throws Exception {
    System.setProperty("foo", "bar");
    try {
      lifeCycle.bindGroovy("ls", "(system.propls { filter = 'foo' } | { it['VALUE'] })()");
      assertEquals("bar", assertOk("ls"));
    } finally {
      System.clearProperty("foo");
    }
  }

  public void testComplete() throws Exception {
    System.setProperty("foo.bar", "bar");
    System.setProperty("foo.bar2", "bar");
    try {
      CompletionMatch completion = assertComplete("system propget foo");
      assertEquals(2, completion.getValue().getSize());
      assertTrue(completion.getValue().get(".bar") != null);
      assertTrue(completion.getValue().get(".bar2") != null);
    } finally {
      System.clearProperty("foo.bar");
      System.clearProperty("foo.bar2");
    }
  }
  
}
