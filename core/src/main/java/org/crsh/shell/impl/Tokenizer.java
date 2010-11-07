/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.shell.impl;

import org.crsh.command.ScriptException;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Tokenizer {

  /** . */
  private static final int NORMAL = 0;

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

    // Remove any white char first
    while (c != null && Character.isWhitespace(c)) {
      next();
    }

    //
    if (c == null) {
      return Token.EOF;
    } else {
      switch (c) {
        case '+':
          next();
          return Token.PLUS;
        case '|':
          next();
          return Token.PIPE;
        default:
          return parseCommand();
      }
    }
  }

  private Token parseCommand() throws ScriptException {

    // The command chunks
    LinkedList<String> chunks = new LinkedList<String>();

    //
    StringBuilder chunk = new StringBuilder();
    Character lastQuote = null;
    out:
    while (c != null) {
      switch (c) {
        case ' ':
          if (lastQuote == null) {
            if (chunk.length() > 0) {
              chunks.addLast(chunk.toString());
              chunk.setLength(0);
            }
          } else {
            chunk.append(c);
          }
          break;
        case '"':
        case '\'':
          if (lastQuote == null) {
            lastQuote = c;
            chunk.append(c);
          } else if (lastQuote != c) {
            chunk.append(c);
          } else {
            chunk.append(c);
            lastQuote = null;
          }
          break;
        case '+':
        case '|':
          if (lastQuote == null) {
            break out;
          }
        default:
          chunk.append(c);
          break;
      }

      //
      next();
    }

    //
    if (chunk.length() > 0) {
      chunks.addLast(chunk.toString());
    }

    //
    if (lastQuote != null) {
      throw new ScriptException("Quote " + lastQuote + " is not closed");
    }

    //
    return new Token.Command(chunks);
  }
}
