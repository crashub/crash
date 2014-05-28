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
public enum KeyType {

  CHARACTER,

  UP,DOWN,LEFT,RIGHT,
  
  DELETE,BACKSPACE,ENTER,

  UNKNOWN

  ;

  /** . */

  static KeyType map(Operation operation, int[] sequence) {
    switch (operation) {
      case SELF_INSERT:
        if (sequence.length == 1 && sequence[0] >= 32) {
          return CHARACTER;
        }
        break;
      case BACKWARD_CHAR:
        return LEFT;
      case FORWARD_CHAR:
        return RIGHT;
      case PREVIOUS_HISTORY:
        return UP;
      case NEXT_HISTORY:
        return DOWN;
      case BACKWARD_DELETE_CHAR:
        return BACKSPACE;
      case DELETE_CHAR:
        return DELETE;
      case ACCEPT_LINE:
        return ENTER;
    }
    return UNKNOWN;
  }

}
