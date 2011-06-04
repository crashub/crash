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

package org.crsh.cmdline.matcher.impl;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TokenizerTestCase extends TestCase {

  public void testEmpty() throws Exception {
    Tokenizer tokenizer = new Tokenizer("");
    assertFalse(tokenizer.hasNext());
  }

  public void testSpace() throws Exception {
    Tokenizer tokenizer = new Tokenizer(" ");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testWord1() throws Exception {
    Tokenizer tokenizer = new Tokenizer("a");
    assertEquals(new Token.Literal.Word(0, "a"), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testWord2() throws Exception {
    Tokenizer tokenizer = new Tokenizer(" a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Word(1, "a"), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testWord3() throws Exception {
    Tokenizer tokenizer = new Tokenizer("-1");
    assertEquals(new Token.Literal.Word(0, "-1"), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testWord4() throws Exception {
    Tokenizer tokenizer = new Tokenizer("--1");
    assertEquals(new Token.Literal.Word(0, "--1"), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testQuotedWord1() throws Exception {
    Tokenizer tokenizer = new Tokenizer("\"a");
    assertEquals(new Token.Literal.Word(0, "\"a", "a", Termination.DOUBLE_QUOTE), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testQuotedWord2() throws Exception {
    Tokenizer tokenizer = new Tokenizer("\"a \"");
    assertEquals(new Token.Literal.Word(0, "\"a \"", "a ", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testQuotedWord3() throws Exception {
    Tokenizer tokenizer = new Tokenizer("a\" \"b");
    assertEquals(new Token.Literal.Word(0, "a\" \"b", "a b", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testQuotedWord4() throws Exception {
    Tokenizer tokenizer = new Tokenizer("\"-a\"");
    assertEquals(new Token.Literal.Word(0, "\"-a\"", "-a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testQuotedWord5() throws Exception {
    Tokenizer tokenizer = new Tokenizer("\"--a\"");
    assertEquals(new Token.Literal.Word(0, "\"--a\"", "--a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testEmptyShortOption() throws Exception {
    Tokenizer tokenizer = new Tokenizer("-");
    assertEquals(new Token.Literal.Option.Short(0, "-", "-", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testShortOption1() throws Exception {
    Tokenizer tokenizer = new Tokenizer("-a");
    assertEquals(new Token.Literal.Option.Short(0, "-a", "-a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testShortOption2() throws Exception {
    Tokenizer tokenizer = new Tokenizer(" -a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Option.Short(1, "-a", "-a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testEmptyLongOption1() throws Exception {
    Tokenizer tokenizer = new Tokenizer("--");
    assertEquals(new Token.Literal.Option.Long(0, "--", "--", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testEmptyLongOption2() throws Exception {
    Tokenizer tokenizer = new Tokenizer("-- ");
    assertEquals(new Token.Literal.Option.Long(0, "--", "--", Termination.DETERMINED), tokenizer.next());
    assertEquals(new Token.Literal.Whitespace(2, " "), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testLongOption1() throws Exception {
    Tokenizer tokenizer = new Tokenizer("--a");
    assertEquals(new Token.Literal.Option.Long(0, "--a", "--a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testLongOption2() throws Exception {
    Tokenizer tokenizer = new Tokenizer(" --a");
    assertEquals(new Token.Whitespace(0, " "), tokenizer.next());
    assertEquals(new Token.Literal.Option.Long(1, "--a", "--a", Termination.DETERMINED), tokenizer.next());
    assertFalse(tokenizer.hasNext());
  }

  public void testIndex() throws Exception {
    Tokenizer tokenizer = new Tokenizer("a b");
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
