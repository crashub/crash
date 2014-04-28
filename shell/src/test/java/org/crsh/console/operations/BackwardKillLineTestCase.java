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

import javax.swing.*;

/**
 * @author Julien Viet
 */
public class BackwardKillLineTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(KeyStrokes.of("abc def"));
    console.on(Operation.BEGINNING_OF_LINE);
    console.on(Operation.BACKWARD_KILL_LINE);
    assertEquals("abc def", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    assertEquals("", getClipboard());
    console.on(KeyStrokes.RIGHT);
    console.on(KeyStrokes.RIGHT);
    console.on(KeyStrokes.RIGHT);
    assertEquals(3, getCurrentCursor());
    console.on(Operation.BACKWARD_KILL_LINE);
    assertEquals(" def", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    assertEquals("abc", getClipboard());
  }

  // Vi move

  public void testKillLine() throws Exception {
    console.init();
    console.on(KeyStrokes.of("abc def"));
    console.on(Operation.BEGINNING_OF_LINE);
    console.on(KeyStrokes.RIGHT);
    console.on(KeyStrokes.RIGHT);
    console.on(KeyStrokes.RIGHT);
    console.on(Operation.VI_EDITING_MODE);
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.BACKWARD_KILL_LINE);
    assertEquals("c def", getCurrentLine());
    assertEquals(0, getCurrentCursor());
    assertEquals("ab", getClipboard());
  }
}
