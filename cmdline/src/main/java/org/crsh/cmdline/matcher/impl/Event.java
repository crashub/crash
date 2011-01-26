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
import org.crsh.cmdline.ParameterDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Event {

  public abstract static class Parameter<T extends Token.Literal, D extends ParameterDescriptor<?>> extends Event {

    /** . */
    protected final D descriptor;

    /** . */
    protected final List<T> values;

    public Parameter(D descriptor, List<T> values) {
      this.descriptor = descriptor;
      this.values = values;
    }

    public final D getDescriptor() {
      return descriptor;
    }

    public final List<T> getValues() {
      return values;
    }

    public final T peekFirst() {
      return values.isEmpty() ? null : values.get(0);
    }

    public final T peekLast() {
      int size = values.size();
      return size == 0 ? null : values.get(size - 1);
    }

    public final List<String> getStrings() {
      List<String> strings = new ArrayList<String>();
      for (T value : values) {
        strings.add(value.value);
      }
      return strings;
    }

    public abstract int getFrom();

    public abstract int getTo();

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[descriptor=" + descriptor + ",values=" + values +  "]";
    }
  }

  public static final class Option extends Parameter<Token.Literal.Word, OptionDescriptor<?>> {

    /** . */
    private final Token.Literal.Option token;

    Option(OptionDescriptor<?> descriptor, Token.Literal.Option token, List<Token.Literal.Word> values) {
      super(descriptor, values);

      this.token = token;
    }

    public final Token.Literal.Option getToken() {
      return token;
    }

    @Override
    public int getFrom() {
      return token.getFrom();
    }

    @Override
    public int getTo() {
      return values.size() == 0 ? token.getTo() : peekLast().getTo();
    }
  }

  public static final class Argument extends Parameter<Token.Literal, ArgumentDescriptor<?>> {

    Argument(ArgumentDescriptor<?> descriptor, List<Token.Literal> values) throws IllegalArgumentException {
      super(descriptor, values);

      //
      if (values.size() == 0) {
        throw new IllegalArgumentException("No empty values");
      }
    }

    @Override
    public int getFrom() {
      return peekFirst().getFrom();
    }

    @Override
    public int getTo() {
      return peekLast().getTo();
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

  public static abstract class Stop extends Event {

    public abstract int getIndex();

    public static final class Done extends Stop {

      /** . */
      private final int index;

      Done(int index) {
        this.index = index;
      }

      @Override
      public int getIndex() {
        return index;
      }
    }

    public static abstract class Unresolved<T extends Token> extends Stop {

      /** . */
      private final T token;

      Unresolved(T token) {
        this.token = token;
      }

      @Override
      public final int getIndex() {
        return token.getFrom();
      }

      public T getToken() {
        return token;
      }

      public static class NoSuchClassOption extends Unresolved<Token.Literal.Option> {
        NoSuchClassOption(Token.Literal.Option token) {
          super(token);
        }
      }

      public static class NoSuchMethodOption extends Unresolved<Token.Literal.Option> {
        NoSuchMethodOption(Token.Literal.Option token) {
          super(token);
        }
      }

      public static class TooManyArguments extends Unresolved<Token> {
        TooManyArguments(Token token) {
          super(token);
        }
      }
    }
  }
}
