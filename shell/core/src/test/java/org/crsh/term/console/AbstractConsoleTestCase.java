/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.term.console;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractConsoleTestCase extends TestCase {

  protected abstract boolean getSupportsCursorMove();

  private static void assertEquals(CharSequence expected, CharSequence actual) {
    assertEquals(expected.toString(), actual.toString());
  }

  /** . */
  private TestClientOutput client;

  /** . */
  private Console console;
  
  @Override
  protected void setUp() throws Exception {
    resetConsole();
  }
  
  private void resetConsole() {
    console = new Console(client = new TestClientOutput(getSupportsCursorMove()));
  }

  public void testWriterCRLF() throws IOException {
    for (String test : new String[]{"a\n","a\r","a\r\n"}) {
      TestClientOutput output = new TestClientOutput(getSupportsCursorMove());
      Console console = new Console(output);
      console.getWriter().write(test);
      output.assertChars("a\r\n");
      output.assertEmpty();
    }
    for (String test : new String[]{"a\n\n","a\n\r","a\r\r"}) {
      TestClientOutput output = new TestClientOutput(getSupportsCursorMove());
      Console console = new Console(output);
      console.getWriter().write(test);
      output.assertChars("a\r\n\r\n");
      output.assertEmpty();
    }
  }

  public void testNoCR() throws IOException {
    console.getViewReader().append("a");
    assertFalse(console.getReader().hasNext());
    assertEquals(1, console.getReader().getSize());
  }

  public void testReadLine() throws IOException {
    for (String test : new String[]{"a\n","a\r","a\r\n"}) {
      resetConsole();
      console.getViewReader().append(test);
      assertTrue(console.getReader().hasNext());
      assertEquals("a", console.getReader().next());
      assertFalse(console.getReader().hasNext());
      assertEquals(0, console.getReader().getSize());
    }

    //
    for (String test : new String[]{"a\n\n","a\n\r","a\r\r"}) {
      resetConsole();
      console.getViewReader().append(test);
      assertTrue(console.getReader().hasNext());
      assertEquals("a", console.getReader().next());
      assertEquals("", console.getReader().next());
      assertFalse(console.getReader().hasNext());
      assertEquals(0, console.getReader().getSize());
    }
  }

  public void testErase() throws IOException {
    resetConsole();
    console.getViewReader().append("a");
    console.getViewReader().del();
    console.getViewReader().append("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals("b", console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  public void testMoveLeftInsert() throws IOException {
    resetConsole();
    console.getViewReader().append("a");
    console.getViewReader().moveLeft();
    console.getViewReader().append("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveLeftInsert(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftInsert();

  public void testMoveLeftDel() throws IOException {
    resetConsole();
    console.getViewReader().append("ab");
    char expected = console.getViewReader().moveLeft() ? 'a' : 'b';
    assertEquals(expected, console.getViewReader().del());
    if (getSupportsCursorMove()) {
      client.assertChars("b ");
      client.assertEmpty();
    } else {
      client.assertChars("a");
      client.assertEmpty();
    }
    console.getViewReader().append("\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveLeftDel(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftDel();

  public void testMoveRightInsert() throws IOException {
    resetConsole();
    console.getViewReader().append("abc");
    console.getViewReader().moveLeft();
    console.getViewReader().moveLeft();
    console.getViewReader().moveRight();
    console.getViewReader().append("d\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveRightInsert(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightInsert();

  public void testMoveRightDel() throws IOException {
    resetConsole();
    console.getViewReader().append("abc");
    console.getViewReader().moveLeft();
    console.getViewReader().moveLeft();
    console.getViewReader().moveRight();
    console.getViewReader().del();
    console.getViewReader().append("\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveRightDel(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightDel();

  public void testMoveRightAtEndOfLine() throws IOException {
    resetConsole();
    console.getViewReader().append("a");
    console.getViewReader().moveRight();
    console.getViewReader().append("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveRightAtEndOfLine(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightAtEndOfLine();

  public void testMoveLeftAtBeginningOfLine() throws IOException {
    resetConsole();
    console.getViewReader().append("a");
    console.getViewReader().moveLeft();
    console.getViewReader().moveLeft();
    console.getViewReader().append("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(getExpectedMoveLeftAtBeginningOfLine(), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftAtBeginningOfLine();

  public void testClearBuffer() throws Exception {
    resetConsole();
    console.getViewReader().append("a");
    console.clearBuffer();
    assertFalse(console.getReader().hasNext());
    console.getViewReader().append("b\n");
    assertEquals("b", console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }
}
