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

package org.crsh.cmdline.matcher.tokenizer;

import org.crsh.cmdline.Delimiter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator<Token> {

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  /** . */
  private ArrayList<Token> stack;

  /** . */
  private int ptr;

  /** . */
  private Delimiter delimiter;

  public Tokenizer(CharSequence s) {
    this.s = s;
    this.stack = new ArrayList<Token>();
    this.index = 0;
    this.delimiter = null;
  }

  public boolean hasNext() {
    if (ptr < stack.size()) {
      return true;
    } else {
      Token next = parse();
      if (next != null) {
        stack.add(next);
      }
      return next != null;
    }
  }

  private Token parse() {
    Token token = null;
    if (index < s.length()) {
      char c = s.charAt(index);
      int from = index;
      while (true) {
        if (Character.isWhitespace(c)) {
          index++;
          if (index < s.length()) {
            c = s.charAt(index);
          } else {
            break;
          }
        } else {
          break;
        }
      }
      if (index > from) {
        token = new Token.Whitespace(from, s.subSequence(from, index).toString());
      } else {
        State state = new State();
        while (true) {
          if (Character.isWhitespace(c) && state.escape == Escape.NONE) {
            break;
          } else {
            index++;
            state.push(c);
            if (index < s.length()) {
              c = s.charAt(index);
            } else {
              break;
            }
          }
        }
        if (index > from) {
          switch (state.status) {
            case INIT: {
              token = new Token.Literal.Word(from, s.subSequence(from, index).toString(), state.buffer.toString());
              break;
            }
            case WORD: {
              token = new Token.Literal.Word(from, s.subSequence(from, index).toString(), state.buffer.toString());
              break;
            }
            case SHORT_OPTION: {
              token = new Token.Literal.Option.Short(from, s.subSequence(from, index).toString(), state.buffer.toString());
              break;
            }
            case LONG_OPTION: {
              token = new Token.Literal.Option.Long(from, s.subSequence(from, index).toString(), state.buffer.toString());
              break;
            }
            default:
              throw new AssertionError(state.status);
          }
          delimiter = state.escape.delimiter;
          return token;
        }
      }
    }
    return token;
  }

  public Token next() {
    if (hasNext()) {
      return stack.get(ptr++);
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int getIndex() {
    Token peek = peek();
    if (peek != null) {
      return peek.getFrom();
    } else {
      return index;
    }
  }

  public void pushBack() {
    pushBack(1);
  }

  public void pushBack(int count) {
    if (count < 0) {
      throw new IllegalArgumentException();
    }
    if (ptr - count < 0) {
      throw new IllegalStateException("Trying to push back too many tokens");
    } else {
      ptr -= count;
    }
  }

  public Token peek() {
    if (hasNext()) {
      return stack.get(ptr);
    } else {
      return null;
    }
  }

  public Delimiter getDelimiter() {
    return delimiter;
  }
}
