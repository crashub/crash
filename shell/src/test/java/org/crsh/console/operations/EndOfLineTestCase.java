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
public class EndOfLineTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(KeyStrokes.of("abc def"));
    console.on(Operation.BEGINNING_OF_LINE);
    console.on(Operation.END_OF_LINE);
    assertEquals("abc def", getCurrentLine());
    assertEquals(7, getCurrentCursor());
  }

  // Vi move

  // The $ key causes the cursor to move to the end of the line
  public void testEndOfLine1() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("chicken sushimi"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.END_OF_LINE);
    console.on(Operation.VI_APPEND_MODE);
    console.on(KeyStrokes.of(" is tasty!"));
    assertEquals("chicken sushimi is tasty!", getCurrentLine());
  }
}
