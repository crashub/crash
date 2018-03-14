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
public class ForwardCharTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(Operation.SELF_INSERT, 'a');
    console.on(Operation.SELF_INSERT, 'b');
    console.on(Operation.SELF_INSERT, 'c');
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.FORWARD_CHAR);
    assertEquals("abc", getCurrentLine());
    assertEquals(3, getCurrentCursor());
  }

  // Vi move

  public void testForwardChar() throws Exception {
    console.init();
    console.on(Operation.VI_EDITING_MODE);
    console.on(KeyStrokes.of("abc"));
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.FORWARD_CHAR);
    assertEquals(2, getCurrentCursor());
  }

  // Move right
  public void testMoveRight1() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("0123456789"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.VI_INSERTION_MODE);
    console.on(KeyStrokes.X);
    assertEquals("012X3456789", getCurrentLine());
  }

  // Move right use digit arguments.
  public void testMoveRight2() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("0123456789ABCDEFHIJK"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.VI_ARG_DIGIT, '1');
    console.on(Operation.VI_ARG_DIGIT, '2');
    console.on(Operation.FORWARD_CHAR);
    console.on(Operation.VI_INSERTION_MODE);
    console.on(KeyStrokes.X);
    assertEquals("0123456789ABXCDEFHIJK", getCurrentLine());
  }

  // Delete move right
  public void testMoveRight3() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("a bunch of words"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.VI_ARG_DIGIT, '5');
    console.on(Operation.VI_DELETE_TO);
    console.on(Operation.FORWARD_CHAR);
    assertEquals("ch of words", getCurrentLine());
  }
}
