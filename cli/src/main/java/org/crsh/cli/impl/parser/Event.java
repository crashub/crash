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

package org.crsh.cli.impl.parser;

import org.crsh.cli.descriptor.ArgumentDescriptor;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.impl.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;

public abstract class Event {

//  public static final class DoubleDash extends Event {
//
//    /** . */
//    protected final Token.Literal.Option.Long token;
//
//    public DoubleDash(Token.Literal.Option.Long token) {
//      this.token = token;
//    }
//  }

  public abstract static class Parameter<T extends Token.Literal, D extends ParameterDescriptor> extends Event {

    /** . */
    protected final CommandDescriptor<?> command;

    /** . */
    protected final D parameter;

    /** . */
    protected final List<T> values;

    public Parameter(CommandDescriptor<?> command, D parameter, List<T> values) {
      this.command = command;
      this.parameter = parameter;
      this.values = values;
    }

    public CommandDescriptor<?> getCommand() {
      return command;
    }

    public final D getParameter() {
      return parameter;
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
        strings.add(value.getValue());
      }
      return strings;
    }

    public abstract int getFrom();

    public abstract int getTo();

    @Override
    public String toString() {
      return getClass().getSimpleName() + "[descriptor=" + parameter + ",values=" + values +  "]";
    }
  }

  public static final class Option extends Parameter<Token.Literal.Word, OptionDescriptor> {

    /** . */
    private final Token.Literal.Option token;

    Option(CommandDescriptor<?> command, OptionDescriptor descriptor, Token.Literal.Option token, List<Token.Literal.Word> values) {
      super(command, descriptor, values);

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

  public static final class Argument extends Parameter<Token.Literal, ArgumentDescriptor> {

    Argument(CommandDescriptor<?> command, ArgumentDescriptor descriptor, List<Token.Literal> values) throws IllegalArgumentException {
      super(command, descriptor, values);

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

  public abstract static class Subordinate extends Event {

    /** . */
    private final CommandDescriptor<?> descriptor;

    public static final class Implicit extends Subordinate {

      /** . */
      private final Token.Literal trigger;

      public Implicit(CommandDescriptor<?> descriptor, Token.Literal trigger) {
        super(descriptor);
        this.trigger = trigger;
      }

      public Token.Literal getTrigger() {
        return trigger;
      }
    }

    public static final class Explicit extends Subordinate {

      /** . */
      private final Token.Literal.Word token;

      public Explicit(CommandDescriptor<?> descriptor, Token.Literal.Word token) {
        super(descriptor);
        this.token = token;
      }

      public Token.Literal.Word getToken() {
        return token;
      }
    }

    Subordinate(CommandDescriptor<?> descriptor) {
      this.descriptor = descriptor;
    }

    public CommandDescriptor<?> getDescriptor() {
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

      public static final class NoSuchOption extends Unresolved<Token.Literal.Option> {
        public NoSuchOption(Token.Literal.Option token) {
          super(token);
        }
      }

      public static final class TooManyArguments extends Unresolved<Token.Literal> {
        TooManyArguments(Token.Literal token) {
          super(token);
        }
      }
    }
  }
}
