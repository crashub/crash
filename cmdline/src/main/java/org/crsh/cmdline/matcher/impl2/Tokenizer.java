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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class Tokenizer {

  /** . */
  private final CharSequence s;

  /** . */
  private int index;

  Tokenizer(CharSequence s) {
    this.s = s;
    this.index = 0;
  }

  Token next() {

    //
    while (index < s.length() && Character.isWhitespace(s.charAt(index))) {
      index++;
    }

    //
    Token next = null;
    if (index < s.length()) {
      int a = index;
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
      int mark = index;
      while (index < s.length()) {
        c = s.charAt(index);
        if (Character.isWhitespace(c)) {
          break;
        } else {
          index++;
        }
      }
      next = new Token(a, type, s.subSequence(mark, index));
    }

    //
    return next;
  }




}
