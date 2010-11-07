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

package org.crsh.shell.impl;

import junit.framework.TestCase;
import org.crsh.command.SyntaxException;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParserTestCase extends TestCase {

  public void testEmpty() {
    assertNull(new Parser("").parse());
  }

  public void testCommand() {
    Parser p = new Parser("a");
    AST.Expr e = (AST.Expr)p.parse();
    assertEquals(Arrays.asList("a"), e.term.commandDefinition);
    assertNull(e.term.next);
    assertNull(e.next);
  }

  public void testPlus() {
    Parser p = new Parser("a+b");
    AST.Expr e = (AST.Expr)p.parse();
    assertEquals(Arrays.asList("a"), e.term.commandDefinition);
    assertEquals(Arrays.asList("b"), e.term.next.commandDefinition);
    assertNull(e.term.next.next);
    assertNull(e.next);
  }

  public void testPipe() {
    Parser p = new Parser("a|b");
    AST.Expr e = (AST.Expr)p.parse();
    assertEquals(Arrays.asList("a"), e.term.commandDefinition);
    assertNull(e.term.next);
    assertEquals(Arrays.asList("b"), e.next.term.commandDefinition);
    assertNull(e.next.next);
  }

  public void testComplex() {
    Parser p = new Parser("a+b|c");
    AST.Expr e = (AST.Expr)p.parse();
    assertEquals(Arrays.asList("a"), e.term.commandDefinition);
    assertEquals(Arrays.asList("b"), e.term.next.commandDefinition);
    assertNull(e.term.next.next);
    assertEquals(Arrays.asList("c"), e.next.term.commandDefinition);
    assertNull(e.next.next);
  }

  public void testSyntaxException() {
    assertSyntaxException("|");
    assertSyntaxException("+");
    assertSyntaxException("a|");
    assertSyntaxException("a+");
  }

  private void assertSyntaxException(String s) {
    try {
      new Parser(s).parse();
      fail();
    } catch (SyntaxException ignore) {
    }
  }
}
