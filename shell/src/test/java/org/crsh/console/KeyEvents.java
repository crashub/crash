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
package org.crsh.console;

import jline.console.Operation;

/**
 * @author Julien Viet
 */
public class KeyEvents {


  /** . */
  public static final KeyEvent a = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'a');

  /** . */
  public static final KeyEvent b = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'b');

  /** . */
  public static final KeyEvent c = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'c');

  /** . */
  public static final KeyEvent d = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'd');

  /** . */
  public static final KeyEvent n = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'n');

  /** . */
  public static final KeyEvent s = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 's');

  /** . */
  public static final KeyEvent X = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, 'X');

  /** . */
  public static final KeyEvent QUOTE = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, '"');

  /** . */
  public static final KeyEvent UNDERSCORE = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, '_');

  /** . */
  public static final KeyEvent ONE = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, '1');

  /** . */
  public static final KeyEvent BACKSLASH = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, '\\');

  /** . */
  public static final KeyEvent ENTER = new KeyEvent(Operation.ACCEPT_LINE, EditorAction.ENTER, 13);

  /** . */
  public static final KeyEvent SPACE = new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, ' ');

  // *****

  /** . */
  public static final KeyEvent UP = new KeyEvent(Operation.PREVIOUS_HISTORY, EditorAction.HISTORY_PREV);

  /** . */
  public static final KeyEvent LEFT = new KeyEvent(Operation.BACKWARD_CHAR, EditorAction.LEFT);

  /** . */
  public static final KeyEvent RIGHT = new KeyEvent(Operation.FORWARD_CHAR, EditorAction.RIGHT);

  /** . */
  public static final KeyEvent DOWN = new KeyEvent(Operation.NEXT_HISTORY, EditorAction.HISTORY_NEXT);

  /** . */
  public static final KeyEvent DELETE_PREV_CHAR = new KeyEvent(Operation.BACKWARD_DELETE_CHAR, EditorAction.DELETE_PREV_CHAR);

  /** . */
  public static final KeyEvent DELETE_NEXT_CHAR = new KeyEvent(Operation.DELETE_CHAR, EditorAction.DELETE_NEXT_CHAR);

  /** . */
  public static final KeyEvent INTERRUPT = new KeyEvent(Operation.INTERRUPT, EditorAction.INTERRUPT);

  /** . */
  public static final KeyEvent DELETE_BEGINNING = new KeyEvent(Operation.UNIX_LINE_DISCARD, EditorAction.DELETE_BEGINNING);

  /** . */
  public static final KeyEvent DELETE_END = new KeyEvent(Operation.KILL_LINE, EditorAction.DELETE_END);

  /** . */
  public static final KeyEvent DELETE_NEXT_WORD = new KeyEvent(Operation.VI_NEXT_WORD, EditorAction.DELETE_NEXT_WORD);

  /** . */
  public static final KeyEvent MOVE_BEGINNING = new KeyEvent(Operation.BEGINNING_OF_LINE, EditorAction.MOVE_BEGINNING);

  /** . */
  public static final KeyEvent MOVE_END = new KeyEvent(Operation.END_OF_LINE, EditorAction.MOVE_END);

  /** . */
  public static final KeyEvent MOVE_PREV_WORD = new KeyEvent(Operation.BACKWARD_WORD, EditorAction.MOVE_PREV_WORD_AT_BEGINNING);

  /** . */
  public static final KeyEvent MOVE_NEXT_WORD = new KeyEvent(Operation.FORWARD_WORD, EditorAction.MOVE_NEXT_WORD_AFTER_END);

  /** . */
  public static final KeyEvent DELETE_PREV_WORD = new KeyEvent(Operation.UNIX_WORD_RUBOUT, EditorAction.DELETE_PREV_WORD);

  /** . */
  public static final KeyEvent PASTE_BEFORE = new KeyEvent(Operation.VI_PUT, EditorAction.PASTE_BEFORE);

  /** . */
  public static final KeyEvent COMPLETE = new KeyEvent(Operation.COMPLETE, EditorAction.COMPLETE);

}
