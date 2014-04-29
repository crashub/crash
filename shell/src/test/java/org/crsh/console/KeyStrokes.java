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
public class KeyStrokes {


  /** . */
  public static final KeyStroke a = new KeyStroke(Operation.SELF_INSERT, 'a');

  /** . */
  public static final KeyStroke b = new KeyStroke(Operation.SELF_INSERT, 'b');

  /** . */
  public static final KeyStroke c = new KeyStroke(Operation.SELF_INSERT, 'c');

  /** . */
  public static final KeyStroke d = new KeyStroke(Operation.SELF_INSERT, 'd');

  /** . */
  public static final KeyStroke n = new KeyStroke(Operation.SELF_INSERT, 'n');

  /** . */
  public static final KeyStroke s = new KeyStroke(Operation.SELF_INSERT, 's');

  /** . */
  public static final KeyStroke X = new KeyStroke(Operation.SELF_INSERT, 'X');

  /** . */
  public static final KeyStroke QUOTE = new KeyStroke(Operation.SELF_INSERT, '"');

  /** . */
  public static final KeyStroke UNDERSCORE = new KeyStroke(Operation.SELF_INSERT, '_');

  /** . */
  public static final KeyStroke ONE = new KeyStroke(Operation.SELF_INSERT, '1');

  /** . */
  public static final KeyStroke BACKSLASH = new KeyStroke(Operation.SELF_INSERT, '\\');

  /** . */
  public static final KeyStroke ENTER = new KeyStroke(Operation.ACCEPT_LINE, 13);

  /** . */
  public static final KeyStroke SPACE = new KeyStroke(Operation.SELF_INSERT, ' ');

  // *****

  /** . */
  public static final KeyStroke UP = new KeyStroke(Operation.PREVIOUS_HISTORY);

  /** . */
  public static final KeyStroke LEFT = new KeyStroke(Operation.BACKWARD_CHAR);

  /** . */
  public static final KeyStroke RIGHT = new KeyStroke(Operation.FORWARD_CHAR);

  /** . */
  public static final KeyStroke DOWN = new KeyStroke(Operation.NEXT_HISTORY);

  /** . */
  public static final KeyStroke DELETE_PREV_CHAR = new KeyStroke(Operation.BACKWARD_DELETE_CHAR);

  /** . */
  public static final KeyStroke DELETE_NEXT_CHAR = new KeyStroke(Operation.DELETE_CHAR);

  /** . */
  public static final KeyStroke INTERRUPT = new KeyStroke(Operation.INTERRUPT);

  /** . */
  public static final KeyStroke DELETE_BEGINNING = new KeyStroke(Operation.UNIX_LINE_DISCARD);

  /** . */
  public static final KeyStroke DELETE_END = new KeyStroke(Operation.KILL_LINE);

  /** . */
  // public static final KeyStroke DELETE_NEXT_WORD = new KeyStroke(Operation.VI_NEXT_WORD);

  /** . */
  public static final KeyStroke MOVE_BEGINNING = new KeyStroke(Operation.BEGINNING_OF_LINE);

  /** . */
  public static final KeyStroke MOVE_END = new KeyStroke(Operation.END_OF_LINE);

  /** . */
  public static final KeyStroke MOVE_PREV_WORD = new KeyStroke(Operation.BACKWARD_WORD);

  /** . */
  public static final KeyStroke MOVE_NEXT_WORD = new KeyStroke(Operation.FORWARD_WORD);

  /** . */
  public static final KeyStroke DELETE_PREV_WORD = new KeyStroke(Operation.UNIX_WORD_RUBOUT);

  /** . */
  // public static final KeyStroke PASTE_BEFORE = new KeyStroke(Operation.VI_PUT);

  /** . */
  public static final KeyStroke COMPLETE = new KeyStroke(Operation.COMPLETE);

  public static KeyStroke of(int value) {
    return new KeyStroke(Operation.SELF_INSERT, value);
  }

  public static KeyStroke[] of(CharSequence value) {
    int len = value.length();
    KeyStroke[] keyStrokes = new KeyStroke[len];
    for (int i = 0;i < len;i++) {
      keyStrokes[i] = of(Character.codePointAt(value, i));
    }
    return keyStrokes;
  }
}
