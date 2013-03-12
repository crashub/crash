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

package org.crsh.cmdline.tokenizer;

class State {

  /** . */
  StringBuilder buffer;

  /** . */
  Escape escape;

  /** . */
  Status status;

  public State() {
    this.buffer = new StringBuilder();
    this.status = Status.INIT;
    this.escape = Escape.NONE;
  }

  void push(char c) {

    //
    switch (escape) {
      case NONE:
        if (c == '"') {
          escape = Escape.DOUBLE;
          return;
        } else if (c == '\\') {
          escape = Escape.BACKSLASH;
          return;
        } else if (c == '\'') {
          escape = Escape.SINGLE;
          return;
        } else {
          // Do nothing
          break;
        }
      case DOUBLE:
        if (c == '"') {
          escape = Escape.NONE;
          return;
        } else {
          // Do nothing
          break;
        }
      case SINGLE:
        if (c == '\'') {
          escape = Escape.NONE;
          return;
        } else {
          // Do nothing
          break;
        }
      case BACKSLASH:
        escape = Escape.NONE;
        break;
      default:
        throw new AssertionError(escape);
    }

    switch (status) {
      case INIT: {
        if (c == '-') {
          buffer.append(c);
          status = Status.SHORT_OPTION;
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      case WORD: {
        buffer.append(c);
        status = Status.WORD;
        return;
      }
      case SHORT_OPTION: {
        if (Character.isLetter(c)) {
          buffer.append(c);
          return;
        } else if (c == '-') {
          buffer.append('-');
          status = Status.LONG_OPTION;
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      case LONG_OPTION: {
        if (Character.isLetter(c) || (buffer.length() > 0 && c == '-')) {
          buffer.append(c);
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      default:
        throw new AssertionError(escape);
    }
  }
}
