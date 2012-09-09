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
import org.crsh.command.ScriptException;

public class TokenizerTestCase extends TestCase {

  public void testEmpty() {
    new TestTokenizer("").assertEOF();
    new TestTokenizer(" ").assertCommand(" ").assertEOF();
  }

  public void testCommand() {
    new TestTokenizer("a").assertCommand("a").assertEOF();
    new TestTokenizer("' '").assertCommand("' '").assertEOF();
    new TestTokenizer("\" \"").assertCommand("\" \"").assertEOF();
    new TestTokenizer("'\"'").assertCommand("'\"'").assertEOF();
    new TestTokenizer("\"'\"").assertCommand("\"'\"").assertEOF();
    new TestTokenizer(" ' ' ").assertCommand(" ' ' ").assertEOF();

    // Test escape special char in simple quote
    new TestTokenizer("'+'").assertCommand("'+'").assertEOF();
    new TestTokenizer("'|'").assertCommand("'|'").assertEOF();

    // Test escape special char in double quote
    new TestTokenizer("\"+\"").assertCommand("\"+\"").assertEOF();
    new TestTokenizer("\"|\"").assertCommand("\"|\"").assertEOF();

    // Non terminated quotes
    new TestTokenizer("\"").assertCommand("\"").assertEOF();
    new TestTokenizer("'").assertCommand("'").assertEOF();

    //
    new TestTokenizer("a b").assertCommand("a b").assertEOF();
  }

  public void testPlus() {
    new TestTokenizer("+").assertPlus().assertEOF();
  }

  public void testPipe() {
    new TestTokenizer("|").assertPipe().assertEOF();
  }

  public void testComposite() {
    TestTokenizer tokenizer = new TestTokenizer("a | b c + d");
    tokenizer.assertCommand("a ");
    tokenizer.assertPipe();
    tokenizer.assertCommand(" b c ");
    tokenizer.assertPlus();
    tokenizer.assertCommand(" d");
    tokenizer.assertEOF();
  }

  private static class TestTokenizer extends Tokenizer {

    private TestTokenizer(CharSequence s) throws NullPointerException {
      super(s);
    }

    public TestTokenizer assertPipe() {
      assertEquals(Token.PIPE, nextToken());
      return this;
    }

    public TestTokenizer assertPlus() {
      assertEquals(Token.PLUS, nextToken());
      return this;
    }

    public void assertEOF() {
      assertEquals(Token.EOF, nextToken());
    }

    public TestTokenizer assertCommand(String line) {
      Token.Command c = (Token.Command)nextToken();
      assertEquals(line, c.line);
      return this;
    }

    public TestTokenizer assertFail() {
      try {
        nextToken();
        fail();
      } catch (ScriptException ignore) {
      }
      return this;
    }
  }
}
