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
package org.crsh.console;

import junit.framework.TestCase;

import java.io.IOException;

public abstract class AbstractEditorBufferTestCase extends TestCase {

  protected abstract boolean getSupportsCursorMove();

  /** . */
  private SimpleConsoleDriver client;

  /** . */
  private EditorBuffer buffer;
  
  @Override
  protected void setUp() throws Exception {
    resetConsole();
  }
  
  private void resetConsole() {
    buffer = new EditorBuffer(client = new SimpleConsoleDriver(getSupportsCursorMove()));
  }

  public void testAppend1() throws IOException {
    buffer.append('a');
    assertFalse(buffer.hasNext());
    assertEquals(1, buffer.getSize());
    assertEquals("a", buffer.getLine());
  }

  public void testAppend2() throws IOException {
    buffer.append("abc");
    assertFalse(buffer.hasNext());
    assertEquals(3, buffer.getSize());
    assertEquals("abc", buffer.getLine());
  }

  public void testAppend3() throws IOException {
    buffer.append("a\rc");
    assertFalse(buffer.hasNext());
    assertEquals(2, buffer.getSize());
    assertEquals("ac", buffer.getLine());
  }

  public void testReadLine() throws IOException {
    for (String test : new String[]{"a\n","a\r\n"}) {
      resetConsole();
      buffer.append(test);
      assertTrue(buffer.hasNext());
      assertEquals("a", buffer.next());
      assertFalse(buffer.hasNext());
      assertEquals(0, buffer.getSize());
    }

    //
    for (String test : new String[]{"a\n\n"}) {
      resetConsole();
      buffer.append(test);
      assertTrue(buffer.hasNext());
      assertEquals("a", buffer.next());
      assertEquals("", buffer.next());
      assertFalse(buffer.hasNext());
      assertEquals(0, buffer.getSize());
    }
  }

  public void testErase() throws IOException {
    resetConsole();
    buffer.append("a");
    buffer.del();
    buffer.append("b\n");
    assertTrue(buffer.hasNext());
    assertEquals("b", buffer.next());
    assertFalse(buffer.hasNext());
  }

  public void testInsert1() throws Exception {
    resetConsole();
    buffer.append("cd", 0, 2);
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.append("ab", 0, 2);
    assertEquals(getInsert1(), buffer.getLine());
    client.assertChars(getInsert1());
  }

  protected abstract String getInsert1();

  public void testInsert2() throws Exception {
    resetConsole();
    buffer.append("cd", 0, 2);
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.append("ab\nef", 0, 5);
    assertEquals("ef", buffer.getLine());
    client.assertChars(getInsert2());
  }

  protected abstract String getInsert2();

  public void testMoveLeftInsert() throws IOException {
    resetConsole();
    buffer.append("a");
    buffer.moveLeft();
    buffer.append("b\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveLeftInsert(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveLeftInsert();

  public void testMoveLeftDel() throws IOException {
    resetConsole();
    buffer.append("ab");
    char expected = buffer.moveLeft() ? 'a' : 'b';
    assertEquals(expected, buffer.del());
    if (getSupportsCursorMove()) {
      client.assertChars("b ");
      client.assertEmpty();
    } else {
      client.assertChars("a");
      client.assertEmpty();
    }
    buffer.append("\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveLeftDel(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveLeftDel();

  public void testMoveRightInsert() throws IOException {
    resetConsole();
    buffer.append("abc");
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.moveRight();
    buffer.append("d\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveRightInsert(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveRightInsert();

  public void testMoveRightDel() throws IOException {
    resetConsole();
    buffer.append("abc");
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.moveRight();
    buffer.del();
    buffer.append("\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveRightDel(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveRightDel();

  public void testMoveRightAtEndOfLine() throws IOException {
    resetConsole();
    buffer.append("a");
    buffer.moveRight();
    buffer.append("b\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveRightAtEndOfLine(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveRightAtEndOfLine();

  public void testMoveRightByTwoChars() throws Exception {
    resetConsole();
    buffer.append("ab");
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.moveRightBy(2);
    buffer.append("c\n");
    assertTrue(buffer.hasNext());
    assertEquals("abc", buffer.next());
    assertFalse(buffer.hasNext());
    client.assertChars("abc");
  }

  public void testMoveLeftAtBeginningOfLine() throws IOException {
    resetConsole();
    buffer.append("a");
    buffer.moveLeft();
    buffer.moveLeft();
    buffer.append("b\n");
    assertTrue(buffer.hasNext());
    assertEquals(getExpectedMoveLeftAtBeginningOfLine(), buffer.next());
    assertFalse(buffer.hasNext());
  }

  protected abstract String getExpectedMoveLeftAtBeginningOfLine();

  public void testClearBuffer() throws Exception {
    resetConsole();
    buffer.append("a");
    buffer.reset();
    assertFalse(buffer.hasNext());
    buffer.append("b\n");
    assertEquals("b", buffer.next());
    assertFalse(buffer.hasNext());
  }
}
