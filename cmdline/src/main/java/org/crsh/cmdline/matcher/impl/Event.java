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

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Event {

  public static final class Option extends Event {

    /** . */
    private final OptionDescriptor<?> descriptor;

    /** . */
    private final Token.Literal.Option token;

    /** . */
    private final List<Token.Literal.Word> values;

    Option(OptionDescriptor<?> descriptor, Token.Literal.Option token, List<Token.Literal.Word> values) {
      this.descriptor = descriptor;
      this.token = token;
      this.values = values;
    }

    public final Token.Literal.Option getToken() {
      return token;
    }

    public final OptionDescriptor<?> getDescriptor() {
      return descriptor;
    }

    public final List<Token.Literal.Word> getValues() {
      return values;
    }

    public final List<String> getStrings() {
      List<String> strings = new ArrayList<String>();
      for (Token.Literal.Word value : values) {
        strings.add(value.value);
      }
      return strings;
    }

    @Override
    public final String toString() {
      return "Event.Option[descriptor=" + descriptor + ",values=" + values +  "]";
    }
  }

  public static final class Argument extends Event {

    /** . */
    private final ArgumentDescriptor<?> descriptor;

    /** . */
    private final List<Token.Literal> values;

    Argument(ArgumentDescriptor<?> descriptor, List<Token.Literal> values) {
      this.descriptor = descriptor;
      this.values = values;
    }

    public ArgumentDescriptor<?> getDescriptor() {
      return descriptor;
    }

    public List<Token.Literal> getValues() {
      return values;
    }

    public List<String> getStrings() {
      List<String> strings = new ArrayList<String>();
      for (Token.Literal value : values) {
        strings.add(value.value);
      }
      return strings;
    }

    @Override
    public String toString() {
      return "Event.Argument[descriptor=" + descriptor + ",values=" + values +  "]";
    }
  }

  public static final class Separator extends Event {

    /** . */
    private final Token.Whitespace token;

    Separator(Token.Whitespace token) {
      this.token = token;
    }

    public Token.Whitespace getToken() {
      return token;
    }
  }

  public abstract static class Method extends Event {

    /** . */
    private final MethodDescriptor<?> descriptor;

    public static final class Implicit extends Method {

      /** . */
      private final Token.Literal trigger;

      public Implicit(MethodDescriptor<?> descriptor, Token.Literal trigger) {
        super(descriptor);
        this.trigger = trigger;
      }

      public Token.Literal getTrigger() {
        return trigger;
      }
    }

    public static final class Explicit extends Method {

      /** . */
      private final Token.Literal.Word token;

      public Explicit(MethodDescriptor<?> descriptor, Token.Literal.Word token) {
        super(descriptor);
        this.token = token;
      }

      public Token.Literal.Word getToken() {
        return token;
      }
    }

    Method(MethodDescriptor<?> descriptor) {
      this.descriptor = descriptor;
    }

    public MethodDescriptor<?> getDescriptor() {
      return descriptor;
    }
  }

  public static final class End extends Event {

    /** . */
    private final Code code;

    /** . */
    private final int index;

    End(Code code, int index) {
      this.code = code;
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    public Code getCode() {
      return code;
    }
  }

}
