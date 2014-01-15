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
package org.crsh.cli.impl.tokenizer;

import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.Quoting;

import java.util.LinkedList;

/**
 * @author Julien Viet
 */
class Automaton extends LineParser.Visitor {

  /** . */
  private Status status = Status.WHITESPACE;

  /** . */
  private final StringBuilder buffer = new StringBuilder();

  /** . */
  final LinkedList<Token> tokens = new LinkedList<Token>();

  /** . */
  private int from = 0;

  /** . */
  private int lastWhitespace = 0;

  /** . */
  private final CharSequence s;

  Automaton(CharSequence s) {
    this.s = s;
  }

  void close() {
    if (buffer.length() > 0) {
      if (status == Status.WHITESPACE) {
        next(lastWhitespace);
      } else {
        next(s.length());
      }
    }
  }

  private void next(int current) {
    Token token;
    switch (status) {
      case WHITESPACE:
        token = new Token.Whitespace(from, s.subSequence(from, current).toString());
        break;
      case WORD:
        token = new Token.Literal.Word(from, s.subSequence(from, current).toString(), buffer.toString());
        break;
      case SHORT_OPTION:
        token = new Token.Literal.Option.Short(from, s.subSequence(from, current).toString(), buffer.toString());
        break;
      case LONG_OPTION:
        token = new Token.Literal.Option.Long(from, s.subSequence(from, current).toString(), buffer.toString());
        break;
      default:
        throw new AssertionError();
    }
    tokens.add(token);
    status = Status.WHITESPACE;
    buffer.setLength(0);
    from = current;
  }

  @Override
  public void onChar(int index, Quoting quoting, boolean backslash, char c) {
    if (Character.isWhitespace(c) && quoting == null && !backslash) {
      lastWhitespace = index + 1;
      if (status != Status.WHITESPACE) {
        next(index);
      }
      buffer.append(c);
    } else {
      switch (status) {
        case WHITESPACE:
          if (buffer.length() > 0) {
            next(lastWhitespace);
          }
          buffer.append(c);
          if (c == '-') {
            status = Status.SHORT_OPTION;
          } else {
            status = Status.WORD;
          }
          break;
        case WORD:
          buffer.append(c);
          break;
        case SHORT_OPTION:
          if (c == '-') {
            buffer.append('-');
            status = Status.LONG_OPTION;
          } else if (Character.isLetter(c)) {
            buffer.append(c);
          } else {
            status = Status.WORD;
            buffer.append(c);
          }
          break;
        case LONG_OPTION:
          if (c == '-') {
            if (buffer.length() > 2) {
              buffer.append(c);
            } else {
              status = Status.WORD;
              buffer.append(c);
            }
          } else if (Character.isLetter(c)) {
            buffer.append(c);
          } else {
            status = Status.WORD;
            buffer.append(c);
          }
      }
    }
  }

  enum Status { WHITESPACE, WORD, SHORT_OPTION, LONG_OPTION }

}
