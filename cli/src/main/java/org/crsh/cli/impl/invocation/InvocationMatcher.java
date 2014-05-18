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

package org.crsh.cli.impl.invocation;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.LiteralValue;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.impl.tokenizer.Token;
import org.crsh.cli.impl.tokenizer.Tokenizer;
import org.crsh.cli.impl.tokenizer.TokenizerImpl;
import org.crsh.cli.impl.parser.Event;
import org.crsh.cli.impl.parser.Mode;
import org.crsh.cli.impl.parser.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationMatcher<T> {

  /** . */
  private final CommandDescriptor<T> descriptor;

  /** . */
  private Iterable<Token> tokens;

  public InvocationMatcher(CommandDescriptor<T> descriptor) {
    this(descriptor, Collections.<Token>emptyList());
  }

  private InvocationMatcher(CommandDescriptor<T> descriptor, Iterable<Token> tokens) {
    this.descriptor = descriptor;
    this.tokens = tokens;
  }

  public InvocationMatcher<T> subordinate(String name) throws SyntaxException {
    TokenList tokens = new TokenList(this.tokens);
    if (name != null && name.length() > 0) {
      tokens.add(new Token.Literal.Word(tokens.last(), name));
    }
    return new InvocationMatcher<T>(descriptor, tokens);
  }

  public InvocationMatcher<T> option(String optionName, List<?> optionValue) throws SyntaxException {
    return options(Collections.<String, List<?>>singletonMap(optionName, optionValue));
  }

  public InvocationMatcher<T> options(Map<String, List<?>> options) throws SyntaxException {
    TokenList tokens = new TokenList(this.tokens);
    for (Map.Entry<String, List<?>> option : options.entrySet()) {
      tokens.addOption(option.getKey(), option.getValue());
    }
    return new InvocationMatcher<T>(descriptor, tokens);
  }

  public InvocationMatch<T> arguments(List<?> arguments) throws SyntaxException {
    TokenList tokens = new TokenList(this.tokens);
    for (Object argument : arguments) {
      tokens.add(new Token.Literal.Word(tokens.last(), argument.toString()));
    }
    return match(tokens);
  }

  public InvocationMatch<T> parse(String s) throws SyntaxException {
    ArrayList<Token> tokens = new ArrayList<Token>();
    for (Token token : this.tokens) {
      tokens.add(token);
    }
    for (Iterator<Token> i = new TokenizerImpl(s);i.hasNext();) {
      tokens.add(i.next());
    }
    return match(tokens);
  }

  private InvocationMatch<T> match(final Iterable<Token> tokens) throws SyntaxException {
    Tokenizer tokenizer = new Tokenizer() {

      /** . */
      Iterator<Token> i = tokens.iterator();

      @Override
      protected Token parse() {
        return i.hasNext() ? i.next() : null;
      }
    };
    return match(tokenizer);
  }

  private InvocationMatch<T> match(Tokenizer tokenizer) throws SyntaxException {

    //
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, Mode.INVOKE);
    InvocationMatch<T> current = new InvocationMatch<T>(descriptor);

    //
    while (true) {
      Event event = parser.next();
      if (event instanceof Event.Separator) {
        //
      } else if (event instanceof Event.Stop) {
        break;
      } else if (event instanceof Event.Option) {
        Event.Option optionEvent = (Event.Option)event;
        OptionDescriptor desc = optionEvent.getParameter();
        Iterable<OptionMatch> options = current.options();
        OptionMatch option = null;
        for (OptionMatch om : options) {
          if (om.getParameter().equals(desc)) {
            List<LiteralValue> v = new ArrayList<LiteralValue>(om.getValues());
            v.addAll(bilto(optionEvent.getValues()));
            List<String> names = new ArrayList<String>(om.getNames());
            names.add(optionEvent.getToken().getName());
            option = new OptionMatch(desc, names, v);
            break;
          }
        }
        if (option == null) {
          option = new OptionMatch(desc, optionEvent.getToken().getName(), bilto(optionEvent.getValues()));
        }
        current.option(option);
      } else if (event instanceof Event.Subordinate) {
        current = current.subordinate(((Event.Subordinate)event).getDescriptor().getName());
      } else if (event instanceof Event.Argument) {
        Event.Argument argumentEvent = (Event.Argument)event;
        List<Token.Literal> values = argumentEvent.getValues();
        ArgumentMatch match;
        if (values.size() > 0) {
          match = new ArgumentMatch(
              argumentEvent.getParameter(),
              argumentEvent.getFrom(),
              argumentEvent.getTo(),
              bilto(argumentEvent.getValues())
          );
          if (argumentEvent.getCommand() == current.getDescriptor()) {
            current.argument(match);
          } else {
            throw new AssertionError();
          }
        }
      }
    }

    //
    StringBuilder rest = new StringBuilder();
    while (tokenizer.hasNext()) {
      Token token = tokenizer.next();
      rest.append(token.getRaw());
    }
    current.setRest(rest.toString());

    //
    return current;
  }

  private List<LiteralValue> bilto(List<? extends Token.Literal> literals) {
    List<LiteralValue> values = new ArrayList<LiteralValue>(literals.size());
    for (Token.Literal literal : literals) {
      values.add(new LiteralValue(literal.getRaw(), literal.getValue()));
    }
    return values;
  }
}
