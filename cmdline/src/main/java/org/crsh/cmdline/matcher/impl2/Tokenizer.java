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

package org.crsh.cmdline.matcher.impl2;

import javax.xml.stream.events.Characters;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Tokenizer implements Iterator<Token> {

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  /** . */
  private Token next;

  Tokenizer(CharSequence s) {
    this.s = s;
    this.index = 0;
  }

  public Token next() {
    if (hasNext()) {
      Token tmp = next;
      next = null;
      return tmp;
    } else {
      throw new NoSuchElementException();
    }
  }

  public boolean hasNext() {
    if (next == null) {
      next = parse();
    }
    return next != null;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  Token peek() {
    if (hasNext()) {
      return next;
    } else {
      return null;
    }
  }

  private Token parse() {

    //
    while (index < s.length() && Character.isWhitespace(s.charAt(index))) {
      index++;
    }

    //
    Token next = null;
    if (index < s.length()) {
      int mark = index;
      char c;
      TokenType type;
      c = s.charAt(index);
      if (c == '-') {
        index++;
        if (index < s.length()) {
          c = s.charAt(index);
          if (c == '-') {
            index++;
            type = TokenType.LONG_OPTION;
          } else {
            type = TokenType.SHORT_OPTION;
          }
        } else {
          type = TokenType.SHORT_OPTION;
        }
      } else {
        type = TokenType.WORD;
      }

      //
      Character lastQuote = null;
      StringBuilder value = new StringBuilder();
      while (index < s.length()) {
        c = s.charAt(index);
        if (lastQuote == null) {
          if (Character.isWhitespace(c)) {
            break;
          } else {
            if (c == '\'' || c == '"') {
              lastQuote = c;
            } else {
              value.append(c);
            }
            index++;
          }
        } else {
          index++;
          if (c == lastQuote) {
            lastQuote = null;
          } else {
            value.append(c);
          }
        }
      }

      //
      Termination termination = lastQuote == null ? Termination.DETERMINED : lastQuote == '\'' ? Termination.SINGLE_QUOTE : Termination.DOUBLE_QUOTE;

      //
      next = new Token(
        mark, type,
        s.subSequence(mark, index).toString(),
        value.toString(),
        termination);
    }

    //
    return next;
  }
}
