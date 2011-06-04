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

package org.crsh.cmdline.matcher.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Tokenizer implements Iterator<Token> {

  /** . */
  private static final int WORD = 0;

  /** . */
  private static final int SHORT_OPTION = 1;

  /** . */
  private static final int LONG_OPTION = 2;

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  /** . */
  private ArrayList<Token> stack;

  /** . */
  private int ptr;

  Tokenizer(CharSequence s) {
    this.s = s;
    this.index = 0;
    this.stack = new ArrayList<Token>();
    this.ptr = 0;
  }

  public Token next() {
    if (hasNext()) {
      return stack.get(ptr++);
    } else {
      throw new NoSuchElementException();
    }
  }

  public boolean hasNext() {
    if (ptr < stack.size()) {
      return true;
    } else {
      Token next = parse();
      if (next != null) {
        stack.add(next);
        return true;
      } else {
        return false;
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  int getIndex() {
    Token peek = peek();
    if (peek != null) {
      return peek.index;
    } else {
      return index;
    }
  }

  void pushBack() {
    pushBack(1);
  }

  void pushBack(int count) {
    if (count < 0) {
      throw new IllegalArgumentException();
    }
    if (ptr - count < 0) {
      throw new IllegalStateException("Trying to push back too many tokens");
    } else {
      ptr -= count;
    }
  }

  Token peek() {
    if (hasNext()) {
      return stack.get(ptr);
    } else {
      return null;
    }
  }

  private Token parse() {

    //
    Token next = null;

    // Consume any whitespace
    int mark = index;
    while (index < s.length() && Character.isWhitespace(s.charAt(index))) {
      index++;
    }

    //
    if (index > mark) {
      next = new Token.Whitespace(mark, s.subSequence(mark, index).toString());
    } else if (index < s.length()) {

      //
      StringBuilder value = new StringBuilder();

      //
      char c;
      int type;
      c = s.charAt(index);
      if (c == '-') {
        if (index + 1  < s.length()) {
          c = s.charAt(index + 1);
          if (c == '-') {
            if (index + 2 < s.length()) {
              c = s.charAt(index + 2);
              if (Character.isLetter(c) || c == ' ') {
                index += 2;
                value.append("--");
                type = LONG_OPTION;
              } else {
                type = WORD;
              }
            } else {
              index += 2;
              value.append("--");
              type = LONG_OPTION;
            }
          } else if (Character.isLetter(c)) {
            index++;
            value.append('-');
            type = SHORT_OPTION;
          } else {
            type = WORD;
          }
        } else {
          index++;
          value.append('-');
          type = SHORT_OPTION;
        }
      } else {
        type = WORD;
      }

      //
      Character lastQuote = null;
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
      switch (type) {
        case WORD:
          next = new Token.Literal.Word(
            mark,
            s.subSequence(mark, index).toString(),
            value.toString(),
            termination);
          break;
        case SHORT_OPTION:
          next = new Token.Literal.Option.Short(
            mark,
            s.subSequence(mark, index).toString(),
            value.toString(),
            termination);
          break;
        case LONG_OPTION:
          next = new Token.Literal.Option.Long(
            mark,
            s.subSequence(mark, index).toString(),
            value.toString(),
            termination);
          break;
        default:
          throw new AssertionError();
      }
    }

    //
    return next;
  }
}
