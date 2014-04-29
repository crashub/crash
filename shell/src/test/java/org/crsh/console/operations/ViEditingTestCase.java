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
import org.crsh.console.Mode;

/**
 * @author Julien Viet
 */
public class ViEditingTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(Operation.VI_EDITING_MODE);
    assertEquals(Mode.VI_INSERT, console.getMode());
  }

  // Vi

  public void testViMode() throws Exception {
    console.init();
    console.on(KeyStrokes.a);
    assertEquals(Mode.EMACS, console.getMode());
    console.on(Operation.VI_EDITING_MODE);
    driver.assertChar('a').assertFlush().assertEmpty();
    assertEquals(Mode.VI_INSERT, console.getMode());
    console.on(Operation.VI_MOVEMENT_MODE);
    driver.assertMoveLeft().assertFlush().assertEmpty();
    assertEquals(Mode.VI_MOVE, console.getMode());
  }
}
