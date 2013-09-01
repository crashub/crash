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

package org.crsh.lang.script;

import org.crsh.command.ScriptException;

class Tokenizer {

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  /** . */
  private Character c;

  /**
   * Create a new tokenizer.
   *
   * @param s the sequence to tokenize
   * @throws NullPointerException if the sequence is null
   */
  public Tokenizer(CharSequence s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException();
    }
    this.s = s;
    this.index = 0;

    // Initialize state
    // index points to next char to read
    // c = s.charAt(index - 1);
    this.c = index < s.length() ? s.charAt(index++) : null;
  }

  private void next() {
    if (index < s.length()) {
      c = s.charAt(index++);
    } else {
      c = null;
    }
  }

  public Token nextToken() {
    if (c == null) {
      return Token.EOF;
    } else {
      switch (c) {
        case '|':
          next();
          return Token.PIPE;
        default:
          return parseCommand();
      }
    }
  }

  private Token parseCommand() throws ScriptException {

    //
    StringBuilder line = new StringBuilder();

    //
    Character lastQuote = null;
    while (c != null) {
      if (lastQuote == null && (c == '+' || c == '|')) {
        break;
      } else {
        line.append(c);
        switch (c) {
          case '"':
          case '\'':
            if (lastQuote == null) {
              lastQuote = c;
            } else if (lastQuote != c) {
            } else {
              lastQuote = null;
            }
            break;
          default:
            break;
        }
      }

      //
      next();
    }

    //
    return new Token.Command(line.toString());
  }
}
