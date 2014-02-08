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
public class BackwardDeleteCharTestCase extends AbstractConsoleTestCase {

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
    console.on(Operation.SELF_INSERT, 'a');
    console.on(Operation.SELF_INSERT, 'b');
    console.on(Operation.SELF_INSERT, 'c');
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("ab", getCurrentLine());
    assertEquals(2, getCurrentCursor());
  }

  // Vi move

  public void testX() throws Exception {
    console.init();
    console.on(Operation.VI_EDITING_MODE);
    console.on(KeyStrokes.of("abc"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("ac", getCurrentLine());
    assertEquals(1, getCurrentCursor());
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("c", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("c", getCurrentLine());
    assertEquals(0, getCurrentCursor());
  }

  public void testRubout1() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("gross animal stuff"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.BACKWARD_DELETE_CHAR);
    console.on(Operation.BACKWARD_DELETE_CHAR);
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("gross animal ff", getCurrentLine());
  }

  public void testRubout2() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("gross animal stuff"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.VI_ARG_DIGIT, '5');
    console.on(Operation.VI_ARG_DIGIT, '0');
    console.on(Operation.BACKWARD_DELETE_CHAR);
    assertEquals("ff", getCurrentLine());
  }
}
