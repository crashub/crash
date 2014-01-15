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
package org.crsh.cli.impl.line;

import junit.framework.TestCase;
import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.MultiLineVisitor;
import org.crsh.cli.impl.line.ValueLineVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Julien Viet
 */
public class LineParserTestCase extends TestCase {

  /** . */
  LineParser o;

  /** . */
  MultiLineVisitor m;

  /** . */
  ValueLineVisitor v;

  /** . */
  LinkedList<Integer> indexes;

  @Override
  public void setUp() throws Exception {
    LineParser.Visitor indexer = new LineParser.Visitor() {
      public void onChar(int index, Quoting quoting, boolean backslash, char c) { indexes.add(index); }
      public void openStrongQuote(int index) { indexes.add(index); }
      public void closeStrongQuote(int index) { indexes.add(index); }
      public void openWeakQuote(int index) { indexes.add(index); }
      public void closeWeakQuote(int index) { indexes.add(index); }
    };
    o = new LineParser(m = new MultiLineVisitor(), v = new ValueLineVisitor(), indexer);
    indexes = new LinkedList<Integer>();
  }

  // % a
  public void testChar() {
    assertTrue(o.append("a").crlf());
    assertEquals("a", m.getRaw());
    assertEquals("a", v.getEvaluated());
    assertEquals(Arrays.asList(0), indexes);
  }

  // % \
  public void testEndingBackslash() {
    assertFalse(o.append("\\").crlf());
    assertTrue(o.crlf());
    assertEquals("", m.getRaw());
    assertEquals("", v.getEvaluated());
    assertEquals(Collections.<Integer>emptyList(), indexes);
  }

  // % "a"
  public void testStrongQuote() {
    o.append("\"a\"");
    assertTrue(o.crlf());
    assertEquals("\"a\"", m.getRaw());
    assertEquals("a", v.getEvaluated());
    assertEquals(Arrays.asList(0,1,2), indexes);
  }

  // % "a"
  public void testWeakQuote() {
    o.append("'a'");
    assertTrue(o.crlf());
    assertEquals("'a'", m.getRaw());
    assertEquals("a", v.getEvaluated());
    assertEquals(Arrays.asList(0,1,2), indexes);
  }

  // % \a
  public void testQuoteChar() {
    o.append("\\a");
    assertTrue(o.crlf());
    assertEquals("\\a", m.getRaw());
    assertEquals("a", v.getEvaluated());
    assertEquals(Arrays.asList(1), indexes);
  }

  // % \"
  public void testQuoteWeakQuote() {
    o.append("\\\"");
    assertTrue(o.crlf());
    assertEquals("\\\"", m.getRaw());
    assertEquals("\"", v.getEvaluated());
    assertEquals(Arrays.asList(1), indexes);
  }

  // % \'
  public void testQuoteStrongQuote() {
    o.append("\\\'");
    assertTrue(o.crlf());
    assertEquals("\\\'", m.getRaw());
    assertEquals("'", v.getEvaluated());
    assertEquals(Arrays.asList(1), indexes);
  }

  // % "
  // > "
  public void testWeakQuotedCrlf() {
    assertFalse(o.append("\"").crlf());
    assertTrue(o.append("\"").crlf());
    assertEquals("\"\n\"", m.getRaw());
    assertEquals("\n", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1, 2), indexes);
  }

  // % '
  // > '
  public void testStrongQuotedCrlf() {
    assertFalse(o.append("'").crlf());
    assertTrue(o.append("'").crlf());
    assertEquals("'\n'", m.getRaw());
    assertEquals("\n", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1, 2), indexes);
  }

  // % "\a"
  public void testCharQuoteInStrongQuote() {
    assertTrue(o.append("\"\\a\"").crlf());
    assertEquals("\"\\a\"", m.getRaw());
    assertEquals("\\a", v.getEvaluated());
    assertEquals(Arrays.asList(0, 2, 3), indexes);
  }

  // % '\a'
  public void testCharQuoteInWeakQuote() {
    assertTrue(o.append("'\\a'").crlf());
    assertEquals("'\\a'", m.getRaw());
    assertEquals("\\a", v.getEvaluated());
    assertEquals(Arrays.asList(0, 2, 3), indexes);
  }

  // % "'"
  public void testStrongQuoteInWeakQuote() {
    assertTrue(o.append("\"'\"").crlf());
    assertEquals("\"'\"", m.getRaw());
    assertEquals("'", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1, 2), indexes);
  }

  // % '"'
  public void testWeakQuoteInStrongQuote() {
    assertTrue(o.append("'\"'").crlf());
    assertEquals("'\"'", m.getRaw());
    assertEquals("\"", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1, 2), indexes);
  }

  // % "\
  // > "
  public void testEndingBackslashInStrongQuote() {
    assertFalse(o.append("\"\\").crlf());
    assertTrue(o.append("\"").crlf());
    assertEquals("\"\"", m.getRaw());
    assertEquals("", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1), indexes);
  }

  // % '\
  // > '
  public void testEndingBackslashInWeakQuote() {
    assertFalse(o.append("'\\").crlf());
    assertTrue(o.append("'").crlf());
    assertEquals("''", m.getRaw());
    assertEquals("", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1), indexes);
  }

  // % "\""
  public void testEscapeStrongQuoteInStrongQuote1() {
    assertTrue(o.append("\"\\\"\"").crlf());
    assertEquals("\"\\\"\"", m.getRaw());
    assertEquals("\"", v.getEvaluated());
    assertEquals(Arrays.asList(0, 2, 3), indexes);
  }

  // % "\"
  public void testEscapeStrongQuoteInStrongQuote2() {
    assertFalse(o.append("\"\\\"").crlf());
  }

  // % '\'
  public void testEscapeWeakQuoteInWeakQuote() {
    assertTrue(o.append("'\\'").crlf());
    assertEquals("'\\'", m.getRaw());
    assertEquals("\\", v.getEvaluated());
    assertEquals(Arrays.asList(0, 1, 2), indexes);
  }
}
