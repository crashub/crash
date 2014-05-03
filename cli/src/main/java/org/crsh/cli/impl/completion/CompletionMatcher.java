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

package org.crsh.cli.impl.completion;

import org.crsh.cli.descriptor.ArgumentDescriptor;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.completers.EmptyCompleter;
import org.crsh.cli.impl.tokenizer.Token;
import org.crsh.cli.impl.tokenizer.TokenizerImpl;
import org.crsh.cli.impl.parser.Event;
import org.crsh.cli.impl.parser.Mode;
import org.crsh.cli.impl.parser.Parser;
import org.crsh.cli.spi.Completer;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class CompletionMatcher<T> {

  /** . */
  private final CommandDescriptor<T> descriptor;

  public CompletionMatcher(CommandDescriptor<T> descriptor) {
    this.descriptor = descriptor;
  }

  public final CompletionMatch match(String s) throws CompletionException {
    return match(EmptyCompleter.getInstance(), s);
  }

  public CompletionMatch match(Completer completer, String s) throws CompletionException {
    return getCompletion(completer, s).complete();
  }

  private Completion argument(CommandDescriptor<?> method, Completer completer, Delimiter delimiter) {
    List<? extends ArgumentDescriptor> arguments = method.getArguments();
    if (arguments.isEmpty()) {
      return new EmptyCompletion();
    } else {
      ArgumentDescriptor argument = arguments.get(0);
      return new ParameterCompletion("", delimiter, argument, completer);
    }
  }

  private Completion getCompletion(Completer completer, String s) throws CompletionException {

    // Find delimiter
    CommandDescriptor<T> foo = this.descriptor;

    TokenizerImpl tokenizer = new TokenizerImpl(s);
    Delimiter delimiter = tokenizer.getEndingDelimiter();
    Parser<T> parser = new Parser<T>(tokenizer, foo, Mode.COMPLETE);

    // Last non separator event
    Event last = null;
    Event.Separator separator = null;
    Event.Stop stop;

    //
    while (true) {
      Event event = parser.next();
      if (event instanceof Event.Separator) {
        separator = (Event.Separator)event;
      } else if (event instanceof Event.Stop) {
        stop = (Event.Stop)event;
        break;
      } else if (event instanceof Event.Option) {
        last = event;
        separator = null;
      } else if (event instanceof Event.Subordinate) {
        // ABUSE!!! fixme
        foo = (CommandDescriptor<T>)((Event.Subordinate)event).getDescriptor();
        last = event;
        separator = null;
      } else if (event instanceof Event.Argument) {
        last = event;
        separator = null;
      }
    }

    //
    if (stop instanceof Event.Stop.Unresolved.NoSuchOption) {
      Event.Stop.Unresolved.NoSuchOption nso = (Event.Stop.Unresolved.NoSuchOption)stop;
      return new OptionCompletion<T>(foo, nso.getToken());
    } else if (stop instanceof Event.Stop.Unresolved) {
      if (stop instanceof Event.Stop.Unresolved.TooManyArguments) {
        Event.Stop.Unresolved.TooManyArguments tma = (Event.Stop.Unresolved.TooManyArguments)stop;
        return new CommandCompletion<T>(foo, s.substring(stop.getIndex()), delimiter);
      } else {
        return new EmptyCompletion();
      }
    } else if (stop instanceof Event.Stop.Done) {
      // to use ?
    }

    if (last == null) {
      if (foo.getSubordinates().size() > 0) {
        return new CommandCompletion<T>(foo, s.substring(stop.getIndex()), Delimiter.EMPTY);
      } else {
        List<ArgumentDescriptor> args = foo.getArguments();
        if (args.size() > 0) {
          return new ParameterCompletion("", delimiter, args.get(0), completer);
        } else {
          return new EmptyCompletion();
        }
      }
    } else if (last instanceof Event.Option) {
      Event.Option optionEvent = (Event.Option)last;
      List<Token.Literal.Word> values = optionEvent.getValues();
      OptionDescriptor option = optionEvent.getParameter();
      if (separator == null) {
        if (values.size() == 0) {
          return new SpaceCompletion();
        } else if (values.size() <= option.getArity()) {
          Token.Literal.Word word = optionEvent.peekLast();
          return new ParameterCompletion(word.getValue(), delimiter, option, completer);
        } else {
          return new EmptyCompletion();
        }
      } else {
        if (values.size() < option.getArity()) {
          return new ParameterCompletion("", delimiter, option, completer);
        } else {
          return argument(foo, completer, delimiter);
        }
      }
    } else if (last instanceof Event.Argument) {
      Event.Argument eventArgument = (Event.Argument)last;
      ArgumentDescriptor argument = eventArgument.getParameter();
      if (separator != null) {
        switch (argument.getMultiplicity()) {
          case SINGLE:
            List<? extends ArgumentDescriptor> arguments = eventArgument.getCommand().getArguments();
            int index = arguments.indexOf(argument) + 1;
            if (index < arguments.size()) {
              ArgumentDescriptor nextArg = arguments.get(index);
              return new ParameterCompletion("", delimiter, nextArg, completer);
            } else {
              return new EmptyCompletion();
            }
          case MULTI:
            return new ParameterCompletion("", delimiter, argument, completer);
          default:
            throw new AssertionError();
        }
      } else {
        Token.Literal value = eventArgument.peekLast();
        return new ParameterCompletion(value.getValue(), delimiter, argument, completer);
      }
    } else if (last instanceof Event.Subordinate) {
      if (separator != null) {
        return argument(foo, completer, delimiter);
      } else {
        return new SpaceCompletion();
      }
    } else {
      throw new AssertionError();
    }
  }
}
