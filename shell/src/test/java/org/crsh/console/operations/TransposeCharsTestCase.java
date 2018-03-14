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
package org.crsh.console.operations;

import jline.console.Operation;
import org.crsh.console.AbstractConsoleTestCase;
import org.crsh.console.KeyStrokes;

/**
 * @author Julien Viet
 */
public class TransposeCharsTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    doTest();
  }

  public void testInsert() {
    console.init();
    console.toInsert();
    doTest();
  }

  private void doTest() {
    console.on(KeyStrokes.of("abc"));
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abc", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    console.on(KeyStrokes.RIGHT);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bac", getCurrentLine());
    assertEquals(2, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bca", getCurrentLine());
    assertEquals(3, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bac", getCurrentLine());
    assertEquals(3, getCurrentCursor());
  }

  // Vi move

  public void testCtrlT1() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("abcdef"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bacdef", getCurrentLine());
    assertEquals(2, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bcadef", getCurrentLine());
    assertEquals(3, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bcdaef", getCurrentLine());
    assertEquals(4, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bcdeaf", getCurrentLine());
    assertEquals(5, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bcdefa", getCurrentLine());
    assertEquals(5, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("bcdeaf", getCurrentLine());
    assertEquals(5, getCurrentCursor());
  }

  public void testCtrlT2() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("abcdef"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdef", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdef", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    console.on(KeyStrokes.MOVE_END);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdfe", getCurrentLine());
    assertEquals(5, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdef", getCurrentLine());
    assertEquals(5, getCurrentCursor());
  }

  public void testCtrlT3() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("abcdef"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdfe", getCurrentLine());
    assertEquals(5, getCurrentCursor());
    console.on(Operation.TRANSPOSE_CHARS);
    assertEquals("abcdef", getCurrentLine());
    assertEquals(5, getCurrentCursor());
  }

}
