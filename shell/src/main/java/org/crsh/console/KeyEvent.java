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

import java.util.Arrays;

/**
 * A key event.
 *
 * @author Julien Viet
 */
public class KeyEvent {

  /** The underlying operation used for key handlers. */
  final Operation operation;

  /** The type of the key. */
  final EditorAction action;

  /** The char sequence. */
  final int[] sequence;

  KeyEvent(Operation operation, EditorAction action, int... sequence) {
    this.operation = operation;
    this.action = action;
    this.sequence = sequence;
  }

  /**
   * Returns the key type decoded according to the user key mapping.
   *
   * @return the key map
   */
  public EditorAction getAction() {
    return action;
  }

  /**
   * Return the key sequence, the returned array must not be modified.
   *
   * @return the key sequence
   */
  public int[] getSequence() {
    return sequence;
  }

  public static KeyEvent of(int value) {
    return new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, value);
  }

  public static KeyEvent of(CharSequence value) {
    int len = value.length();
    int[] buffer = new int[len];
    for (int i = 0;i < len;i++) {
      buffer[i] = Character.codePointAt(value, i);
    }
    return new KeyEvent(Operation.SELF_INSERT, EditorAction.INSERT, buffer);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof KeyEvent) {
      KeyEvent that = (KeyEvent)obj;
      return action == that.action && Arrays.equals(sequence, that.sequence);
    } else {
      return false;
    }
  }
}
