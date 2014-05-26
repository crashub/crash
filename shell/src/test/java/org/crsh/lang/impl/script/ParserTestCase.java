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

package org.crsh.lang.impl.script;

import junit.framework.TestCase;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;

public class ParserTestCase extends TestCase {

  public void testBlank() throws CommandException {
    assertNull(Token.parse("").createFactory());
    assertNull(Token.parse(" ").createFactory());
  }

  public void testCommand() throws CommandException {
    PipeLineFactory e = Token.parse("a").createFactory();
    assertEquals("a", e.getLine());
    assertNull(e.getNext());
  }

  public void testPipe() throws CommandException {
    PipeLineFactory e = Token.parse("a|b").createFactory();
    assertEquals("a", e.getLine());
    assertEquals("b", e.getNext().getLine());
    assertNull(e.getNext().getNext());
  }

  public void testSyntaxException() {
    assertSyntaxException("|");
    assertSyntaxException("a|");
    assertSyntaxException("|b");
  }

  private void assertSyntaxException(String s) {
    try {
      Token.parse(s).createFactory();
      fail();
    }
    catch (CommandException e) {
      assertEquals(ErrorKind.SYNTAX, e.getErrorKind());
    }
  }
}
