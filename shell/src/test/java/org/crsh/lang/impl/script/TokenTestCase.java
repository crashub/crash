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

public class TokenTestCase extends TestCase {

  public void testEmpty() {
    new TestTokenizer("").assertCommand("").assertEOF();
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
  public void testPipe() {
    new TestTokenizer("|").assertCommand("").assertCommand("").assertEOF();
  }

  public void testComposite() {
    TestTokenizer tokenizer = new TestTokenizer("a | b c");
    tokenizer.assertCommand("a ");
    tokenizer.assertCommand(" b c");
    tokenizer.assertEOF();
  }

  private static class TestTokenizer {

    /** . */
    private Token current;

    private TestTokenizer(CharSequence s) throws NullPointerException {
      current = Token.parse(s);
    }

    public void assertEOF() {
      assertEquals(null, current);
    }

    public TestTokenizer assertCommand(String line) {
      assertEquals(line, current.value);
      current = current.next;
      return this;
    }
  }
}
