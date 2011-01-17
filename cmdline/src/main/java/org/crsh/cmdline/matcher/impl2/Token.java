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
abstract class Token {


  final static class Whitespace extends Token {

    Whitespace(int index, String raw) {
      super(index, raw);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Whitespace) {
        Whitespace that = (Whitespace)obj;
        return super.equals(obj) && index == that.index;
      }
      return false;
    }

    @Override
    public String toString() {
      return "Token.Whitespace[index=" + index + ",raw=" + raw + "]";
    }
  }

  final static class Literal extends Token {

    /** . */
    final TokenType type;

    /** . */
    final String value;

    /** . */
    final Termination termination;

    Literal(int index, TokenType type, String value) {
      this(index, type, value, value, Termination.DETERMINED);
    }

    Literal(int index, TokenType type, String raw, String value, Termination termination) {
      super(index, raw);

      //
      if (type == null) {
        throw new NullPointerException();
      }
      if (value == null) {
        throw new NullPointerException();
      }
      if (termination == null) {
        throw new NullPointerException();
      }

      //
      this.type = type;
      this.value = value;
      this.termination = termination;
    }

    public boolean isOption() {
      switch (type) {
        case SHORT_OPTION:
        case LONG_OPTION:
          return true;
        default:
          return false;
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Literal) {
        Literal that = (Literal)obj;
        return super.equals(obj) && index == that.index && value.equals(that.value) && termination == that.termination;
      }
      return false;
    }

    @Override
    public String toString() {
      return "Token.Literal[index=" + index + ",type=" + type.name() + ",raw=" + raw + ",value=" + value + ",termination=" + termination.name() + "]";
    }
  }

  /** The index in the containing sequence. */
  final int index;

  /** . */
  final String raw;



  Token(int index, String raw) {

    if (index < 0) {
      throw new IllegalArgumentException();
    }
    if (raw == null) {
      throw new NullPointerException();
    }

    //
    this.index = index;
    this.raw = raw;
  }

  /**
   * Returns the from index is the containing string.
   *
   * @return the from index
   */
  public int getFrom() {
    return index;
  }

  /**
   * Returns the to index in the containing string.
   *
   * @return the to index
   */
  public int getTo() {
    return index + raw.length();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Token) {
      Token that = (Token)obj;
      return index == that.index && raw.equals(that.raw);
    }
    return false;
  }
}
