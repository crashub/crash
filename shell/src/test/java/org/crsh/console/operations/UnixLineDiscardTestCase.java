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
import org.crsh.console.KeyEvent;
import org.crsh.console.KeyEvents;

/**
 * @author Julien Viet
 */
public class UnixLineDiscardTestCase extends AbstractConsoleTestCase {

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
    console.on(KeyEvent.of("abcdef"));
    console.on(KeyEvents.LEFT);
    console.on(KeyEvents.LEFT);
    console.on(KeyEvents.LEFT);
    console.on(Operation.UNIX_LINE_DISCARD);
    assertEquals("def", getCurrentLine());
    assertEquals(0, getCurrentCursor());
  }

  // Vi move

  public void testCtrlU1() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyEvent.of("all work and no play"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(KeyEvents.LEFT, KeyEvents.LEFT, KeyEvents.LEFT);
    console.on(Operation.UNIX_LINE_DISCARD);
    assertEquals("play", getCurrentLine());
  }

  public void testCtrlU2() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyEvent.of("donkey punch"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(KeyEvents.MOVE_BEGINNING);
    console.on(Operation.UNIX_LINE_DISCARD);
    assertEquals("donkey punch", getCurrentLine());
  }

  public void testCtrlU3() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyEvent.of("rabid hamster"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.UNIX_LINE_DISCARD);
    assertEquals("r", getCurrentLine());
  }
}
