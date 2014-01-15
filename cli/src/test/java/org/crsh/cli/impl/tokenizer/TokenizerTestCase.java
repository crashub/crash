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

package org.crsh.cli.impl.tokenizer;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TokenizerTestCase extends TestCase {

  private void assertDone(Tokenizer tokenizer) {
    if (tokenizer.hasNext()) {
      Token next = tokenizer.next();
      fail("Was not expecting " + next);
    }
  }

  public void testEmpty() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("");
    assertDone(tokenizer);
  }

  public void testWhitespace1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" ");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWhitespace2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("  ");
    assertEquals(new Token.Whitespace(0, "  "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("a");
    assertEquals(new Token.Literal.Word(0, "a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("ab");
    assertEquals(new Token.Literal.Word(0, "ab"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Word(1, "a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord4() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("-1");
    assertEquals(new Token.Literal.Word(0, "-1"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord5() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("--1");
    assertEquals(new Token.Literal.Word(0, "--1"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testWord6() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("a ");
    assertEquals(new Token.Literal.Word(0, "a"), tokenizer.next());
    assertEquals(new Token.Whitespace(1, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"a");
    assertEquals(new Token.Literal.Word(0, "\"a", "a"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("'a");
    assertEquals(new Token.Literal.Word(0, "'a", "a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"a \"");
    assertEquals(new Token.Literal.Word(0, "\"a \"", "a "), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("\'a \'");
    assertEquals(new Token.Literal.Word(0, "\'a \'", "a "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("a\" \"b");
    assertEquals(new Token.Literal.Word(0, "a\" \"b", "a b"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("a\' \'b");
    assertEquals(new Token.Literal.Word(0, "a\' \'b", "a b"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord4() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"-a\"");
    assertEquals(new Token.Literal.Option.Short(0, "\"-a\"", "-a"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("\'-a\'");
    assertEquals(new Token.Literal.Option.Short(0, "\'-a\'", "-a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord5() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"--a\"");
    assertEquals(new Token.Literal.Option.Long(0, "\"--a\"", "--a"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("\'--a\'");
    assertEquals(new Token.Literal.Option.Long(0, "\'--a\'", "--a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord6() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"'\"");
    assertEquals(new Token.Literal.Option.Word(0, "\"'\"", "'"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("'\"'");
    assertEquals(new Token.Literal.Option.Word(0, "'\"'", "\""), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuotedWord7() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("a\"\"b");
    assertEquals(new Token.Literal.Option.Word(0, "a\"\"b", "ab"), tokenizer.next());
    assertDone(tokenizer);
    tokenizer = new TokenizerImpl("a''b");
    assertEquals(new Token.Literal.Option.Word(0, "a''b", "ab"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testEmptyShortOption() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("-");
    assertEquals(new Token.Literal.Option.Short(0, "-", "-"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testShortOption1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("-a");
    assertEquals(new Token.Literal.Option.Short(0, "-a", "-a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testShortOption2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("-ab");
    assertEquals(new Token.Literal.Option.Short(0, "-ab", "-ab"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testShortOption3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" -a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Option.Short(1, "-a", "-a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testShortOption4() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("- ");
    assertEquals(new Token.Literal.Option.Short(0, "-", "-"), tokenizer.next());
    assertEquals(new Token.Whitespace(1, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testEmptyLongOption1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("--");
    assertEquals(new Token.Literal.Option.Long(0, "--", "--"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testEmptyLongOption2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("-- ");
    assertEquals(new Token.Literal.Option.Long(0, "--", "--"), tokenizer.next());
    assertEquals(new Token.Literal.Whitespace(2, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testLongOption1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("--a");
    assertEquals(new Token.Literal.Option.Long(0, "--a", "--a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testLongOption2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("--ab");
    assertEquals(new Token.Literal.Option.Long(0, "--ab", "--ab"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testLongOption3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" --a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Option.Long(1, "--a", "--a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testLongOptionWithHyphen() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("--a-b");
    assertEquals(new Token.Literal.Option.Long(0, "--a-b", "--a-b"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testBackSlash1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\\a");
    assertEquals(new Token.Literal.Word(0, "\\a", "a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testBackSlash2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\\ ");
    assertEquals(new Token.Literal.Word(0, "\\ ", " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testBackSlash3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\\-a");
    assertEquals(new Token.Literal.Option.Short(0, "\\-a", "-a"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testEmptyWord1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"\"");
    assertDone(tokenizer);
  }

  public void testEmptyWord2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" \"\"");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testEmptyWord3() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("\"\"\"\"");
    assertDone(tokenizer);
  }

  public void testQuoteAfterWhitespace1() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" \"\"");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testQuoteAfterWhitespace2() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl(" \"b\"");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Word(1, "\"b\"", "b"), tokenizer.next());
    assertDone(tokenizer);
  }

  public void testIndex() throws Exception {
    Tokenizer tokenizer = new TokenizerImpl("a b");
    assertEquals(0, tokenizer.getIndex());
    assertEquals(new Token.Literal.Word(0, "a"), tokenizer.next());
    assertEquals(1, tokenizer.getIndex());
    assertEquals(new Token.Literal.Whitespace(1, " "), tokenizer.next());
    assertEquals(2, tokenizer.getIndex());
    tokenizer.pushBack(1);
    assertEquals(1, tokenizer.getIndex());
    assertEquals(new Token.Literal.Whitespace(1, " "), tokenizer.next());
    assertEquals(2, tokenizer.getIndex());
  }
}
