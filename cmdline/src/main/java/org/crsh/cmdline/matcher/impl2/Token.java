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
class Token {

  /** The index in the containing sequence. */
  final int index;

  /** . */
  final TokenType type;

  /** . */
  final CharSequence value;

  Token(int index, TokenType type, CharSequence value) {

    if (index < 0) {
      throw new IllegalArgumentException();
    }
    if (type == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }

    //
    this.index = index;
    this.type = type;
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Token) {
      Token that = (Token)obj;
      return index == that.index && type == that.type && value.toString().equals(that.value.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    return "Token[index=" + index + ",type=" + type.name() + ",value=" + value + "]";
  }
}
