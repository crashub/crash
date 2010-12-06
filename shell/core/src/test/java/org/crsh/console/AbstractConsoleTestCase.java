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

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractConsoleTestCase extends TestCase {

  protected abstract boolean getSupportsCursorMove();

  private Console newConsole() {
    return new Console(new TestEcho(getSupportsCursorMove()));

  }

  public void testNoCR() throws IOException {
    Console console = newConsole();
    console.getInput().write("a");
    assertFalse(console.getReader().hasNext());
    assertEquals(1, console.getReader().getSize());
  }

  public void testReadLine() throws IOException {
    for (String test : new String[]{"a\n","a\r","a\r\n"}) {
      Console console = newConsole();
      console.getInput().write(test);
      assertTrue(console.getReader().hasNext());
      assertEquals(new org.crsh.console.Input.Chars("a"), console.getReader().next());
      assertFalse(console.getReader().hasNext());
      assertEquals(0, console.getReader().getSize());
    }

    //
    for (String test : new String[]{"a\n\n","a\n\r","a\r\r"}) {
      Console console = newConsole();
      console.getInput().write(test);
      assertTrue(console.getReader().hasNext());
      assertEquals(new org.crsh.console.Input.Chars("a"), console.getReader().next());
      assertEquals(new org.crsh.console.Input.Chars(""), console.getReader().next());
      assertFalse(console.getReader().hasNext());
      assertEquals(0, console.getReader().getSize());
    }
  }

  public void testErase() throws IOException {
    Console console = newConsole();
    console.getInput().write("a");
    console.getInput().del();
    console.getInput().write("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars("b"), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  public void testMoveLeftInsert() throws IOException {
    Console console = newConsole();
    console.getInput().write("a");
    console.getInput().moveLeft();
    console.getInput().write("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveLeftInsert()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftInsert();

  public void testMoveLeftDel() throws IOException {
    Console console = newConsole();
    console.getInput().write("ab");
    console.getInput().moveLeft();
    console.getInput().del();
    console.getInput().write("\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveLeftDel()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftDel();

  public void testMoveRightInsert() throws IOException {
    Console console = newConsole();
    console.getInput().write("abc");
    console.getInput().moveLeft();
    console.getInput().moveLeft();
    console.getInput().moveRight();
    console.getInput().write("d\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveRightInsert()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightInsert();

  public void testMoveRightDel() throws IOException {
    Console console = newConsole();
    console.getInput().write("abc");
    console.getInput().moveLeft();
    console.getInput().moveLeft();
    console.getInput().moveRight();
    console.getInput().del();
    console.getInput().write("\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveRightDel()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightDel();

  public void testMoveRightAtEndOfLine() throws IOException {
    Console console = newConsole();
    console.getInput().write("a");
    console.getInput().moveRight();
    console.getInput().write("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveRightAtEndOfLine()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveRightAtEndOfLine();

  public void testMoveLeftAtBeginningOfLine() throws IOException {
    Console console = newConsole();
    console.getInput().write("a");
    console.getInput().moveLeft();
    console.getInput().moveLeft();
    console.getInput().write("b\n");
    assertTrue(console.getReader().hasNext());
    assertEquals(new Input.Chars(getExpectedMoveLeftAtBeginningOfLine()), console.getReader().next());
    assertFalse(console.getReader().hasNext());
  }

  protected abstract String getExpectedMoveLeftAtBeginningOfLine();
}
