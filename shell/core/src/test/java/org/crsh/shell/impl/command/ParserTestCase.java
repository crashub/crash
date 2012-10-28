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

package org.crsh.shell.impl.command;

import junit.framework.TestCase;
import org.crsh.command.SyntaxException;

public class ParserTestCase extends TestCase {

  public void testEmpty() {
    assertNull(new PipeLineParser("").parse());
  }

  public void testCommand() {
    PipeLineParser p = new PipeLineParser("a");
    PipeLineFactory e = p.parse();
    assertEquals("a", e.line);
    assertNull(e.next);
  }

  public void testPipe() {
    PipeLineParser p = new PipeLineParser("a|b");
    PipeLineFactory e = p.parse();
    assertEquals("a", e.line);
    assertEquals("b", e.next.line);
    assertNull(e.next.next);
  }

  public void testSyntaxException() {
    assertSyntaxException("|");
    assertSyntaxException("a|");
  }

  private void assertSyntaxException(String s) {
    try {
      new PipeLineParser(s).parse();
      fail();
    } catch (SyntaxException ignore) {
    }
  }
}
