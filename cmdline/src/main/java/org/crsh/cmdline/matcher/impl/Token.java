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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Token {


  public final static class Whitespace extends Token {

    public Whitespace(int index, String raw) {
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

  public abstract static class Literal extends Token {

    public abstract static class Option extends Literal {

      /** . */
      private final String name;

      public final String getName() {
        return name;
      }

      public Option(int index, String raw, String value, Termination termination, String name) {
        super(index, raw, value, termination);
        this.name = name;
      }

      public final static class Short extends Option {
        public Short(int index, String raw, String value, Termination termination) {
          super(index, raw, value, termination, value.substring(1));
        }
      }

      public final static class Long extends Option {
        public Long(int index, String raw, String value, Termination termination) {
          super(index, raw, value, termination, value.substring(2));
        }
      }
    }

    public final static class Word extends Literal {
      public Word(int index, String raw, String value, Termination termination) {
        super(index, raw, value, termination);
      }

      public Word(int index, String value) {
        super(index, value);
      }
    }

    /** . */
    final String value;

    /** . */
    final Termination termination;

    public Literal(int index, String value) {
      this(index, value, value, Termination.DETERMINED);
    }

    public Literal(int index, String raw, String value, Termination termination) {
      super(index, raw);

      if (value == null) {
        throw new NullPointerException();
      }
      if (termination == null) {
        throw new NullPointerException();
      }

      //
      this.value = value;
      this.termination = termination;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj.getClass().equals(getClass())) {
        Literal that = (Literal)obj;
        return super.equals(obj) && index == that.index && value.equals(that.value) && termination == that.termination;
      }
      return false;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[index=" + index + ",raw=" + raw + ",value=" + value + ",termination=" + termination.name() + "]";
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
