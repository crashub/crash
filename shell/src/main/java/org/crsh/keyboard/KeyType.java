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
package org.crsh.keyboard;

/**
 * The type of a key.
 *
 * @author Julien Viet
 */
public enum KeyType {

  /** The key is a character and the value is the actual char code point. */
  CHARACTER,

  /** Up arrow . */
  UP,

  /** Down arrow . */
  DOWN,

  /** Left arrow . */
  LEFT,

  /** Right arrow . */
  RIGHT,

  /** Delete right char. */
  DELETE,

  /** Delete left char. */
  BACKSPACE,

  /** Enter. */
  ENTER,

  /** Anything not categorizable. */
  UNKNOWN

}
