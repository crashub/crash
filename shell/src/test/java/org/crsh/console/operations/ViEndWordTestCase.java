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

import java.io.IOException;

/**
 * @author Julien Viet
 */
public class ViEndWordTestCase extends AbstractConsoleTestCase {

  public void testEndWord1() throws IOException {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("putrid pidgen porridge"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.VI_END_WORD);
    console.on(Operation.KILL_LINE);
    assertEquals("putri", getCurrentLine());
    assertEquals(4, getCurrentCursor());
  }

  public void testEndWord2() throws IOException {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("    putrid pidgen porridge"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(Operation.VI_END_WORD);
    console.on(Operation.KILL_LINE);
    assertEquals("    putri", getCurrentLine());
    assertEquals(8, getCurrentCursor());
  }

}
