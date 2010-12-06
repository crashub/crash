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
package org.crsh.console;

import junit.framework.TestCase;
import org.crsh.util.Input;
import org.crsh.util.TestInputDecoder;

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractConsoleTestCase extends TestCase {

  protected abstract boolean getSupportsCursorMove();

  public void testNoCR() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("a");
    assertFalse(sm.hasNext());
    assertEquals(1, sm.getSize());
  }

  public void testReadLine() throws IOException {
    String[] tests = {"a\n","a\r","a\n\r","a\r\n"};
    for (String test : tests) {
      TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
      sm.appendData(test);
      assertTrue(sm.hasNext());
      assertEquals(new org.crsh.util.Input.Chars("a"), sm.next());
      assertFalse(sm.hasNext());
      assertEquals(0, sm.getSize());
    }
  }

  public void testErase() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("a");
    sm.appendDel();
    sm.appendData("b\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars("b"), sm.next());
    assertFalse(sm.hasNext());
  }

  public void testMoveLeftInsert() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("a");
    sm.moveLeft();
    sm.appendData("b\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars(getExpectedMoveLeftInsert()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveLeftInsert();

  public void testMoveLeftDel() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("ab");
    sm.moveLeft();
    sm.appendDel();
    sm.appendData("\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars(getExpectedMoveLeftDel()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveLeftDel();

  public void testMoveRightInsert() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("abc");
    sm.moveLeft();
    sm.moveLeft();
    sm.moveRight();
    sm.appendData("d\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars(getExpectedMoveRightInsert()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveRightInsert();

  public void testMoveRightDel() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("abc");
    sm.moveLeft();
    sm.moveLeft();
    sm.moveRight();
    sm.appendDel();
    sm.appendData("\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars(getExpectedMoveRightDel()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveRightDel();

  public void testMoveRightAtEndOfLine() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("a");
    sm.moveRight();
    sm.appendData("b\n");
    assertTrue(sm.hasNext());
    assertEquals(new org.crsh.util.Input.Chars(getExpectedMoveRightAtEndOfLine()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveRightAtEndOfLine();

  public void testMoveLeftAtBeginningOfLine() throws IOException {
    TestInputDecoder sm = new TestInputDecoder(getSupportsCursorMove());
    sm.appendData("a");
    sm.moveLeft();
    sm.moveLeft();
    sm.appendData("b\n");
    assertTrue(sm.hasNext());
    assertEquals(new Input.Chars(getExpectedMoveLeftAtBeginningOfLine()), sm.next());
    assertFalse(sm.hasNext());
  }

  protected abstract String getExpectedMoveLeftAtBeginningOfLine();
}
