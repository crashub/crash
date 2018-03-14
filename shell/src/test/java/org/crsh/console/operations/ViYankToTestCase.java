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
import org.crsh.console.KeyStrokes;
import org.crsh.console.Mode;

/**
 * @author Julien Viet
 */
public class ViYankToTestCase extends AbstractPasteTestCase {

  public void testNormal() {
    console.init();
    console.on(KeyStrokes.of("abcdef"));
    console.toMove();
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("abcdef", getClipboard());
  }

  public void testDigit() {
    console.init();
    console.on(KeyStrokes.of("abcdef"));
    console.toMove();
    console.on(Operation.VI_ARG_DIGIT, '4');
    assertEquals(4, assertInstance(Mode.Digit.class, console.getMode()).getCount());
    assertEquals(null, assertInstance(Mode.Digit.class, console.getMode()).getTo());
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("abcdef", getClipboard());
  }

  public void testNextWord() {
    console.init();
    console.on(KeyStrokes.of("abc  def"));
    console.toMove();
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.VI_NEXT_WORD);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("bc  ", getClipboard());
    assertEquals(1, getCurrentCursor());
  }

  public void testPrev() {
    console.init();
    console.on(KeyStrokes.of("abc  def"));
    console.toMove();
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.VI_FIRST_PRINT);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("  de", getClipboard());
    assertEquals(7, getCurrentCursor());
  }

  public void testEndOfLine() {
    console.init();
    console.on(KeyStrokes.of("abc  def"));
    console.toMove();
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.END_OF_LINE);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("bc  def", getClipboard());
    assertEquals(1, getCurrentCursor());
  }

  public void testBeginningOfLine() {
    console.init();
    console.on(KeyStrokes.of("abc  def"));
    console.toMove();
    console.on(KeyStrokes.LEFT);
    console.on(KeyStrokes.LEFT);
    console.on(Operation.VI_YANK_TO);
    assertEquals(Mode.YANK_TO, console.getMode());
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    assertEquals(Mode.VI_MOVE, console.getMode());
    assertEquals("abc  d", getClipboard());
    assertEquals(6, getCurrentCursor());
  }
}
