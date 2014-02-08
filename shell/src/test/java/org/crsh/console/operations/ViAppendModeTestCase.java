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
public class ViAppendModeTestCase extends AbstractConsoleTestCase {

  public void testAppendMode1() throws Exception {
    console.init();
    console.on(Operation.VI_EDITING_MODE);
    console.on(KeyStrokes.of("abc"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_APPEND_MODE);
    console.on(KeyStrokes.of("d"));
    assertEquals("abcd", getCurrentLine());
    assertEquals(4, getCurrentCursor());
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.RIGHT);
    assertEquals(3, getCurrentCursor());
  }

  public void testAppendMode2() throws Exception {
    console.init();
    console.on(Operation.VI_EDITING_MODE);
    console.on(KeyStrokes.of("abd"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_APPEND_MODE);
    console.on(KeyStrokes.of("c"));
    assertEquals("abcd", getCurrentLine());
    assertEquals(3, getCurrentCursor());
  }
}
