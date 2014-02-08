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
public class BackwardCharTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(Operation.SELF_INSERT, 'a');
    console.on(Operation.SELF_INSERT, 'b');
    console.on(Operation.SELF_INSERT, 'c');
    console.on(Operation.BACKWARD_CHAR);
    assertEquals("abc", getCurrentLine());
    assertEquals(2, getCurrentCursor());
  }

  // Vi move

  // Move left
  public void testMoveLeft1() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("0123456789"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_INSERTION_MODE);
    console.on(KeyStrokes.X);
    assertEquals("012345X6789", getCurrentLine());
  }

  // Move left - use digit arguments.
  public void testMoveLeft2() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("0123456789"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_ARG_DIGIT, '3');
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.VI_INSERTION_MODE);
    console.on(KeyStrokes.X);
    assertEquals("012345X6789", getCurrentLine());
  }

  // Move left - use multi-digit arguments.
  public void testMoveLeft3() throws Exception {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.of("0123456789ABCDEFHIJLMNOPQRSTUVWXYZ"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_ARG_DIGIT, '1');
    console.on(Operation.VI_ARG_DIGIT, '3');
    console.on(Operation.BACKWARD_CHAR);
    console.on(Operation.VI_INSERTION_MODE);
    console.on(KeyStrokes.X);
    assertEquals("0123456789ABCDEFHIJLXMNOPQRSTUVWXYZ", getCurrentLine());
  }
}
