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

package org.crsh.cmdline.invocation;

import org.crsh.cmdline.SyntaxException;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.LiteralValue;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.tokenizer.Token;
import org.crsh.cmdline.tokenizer.Tokenizer;
import org.crsh.cmdline.tokenizer.TokenizerImpl;
import org.crsh.cmdline.parser.Event;
import org.crsh.cmdline.parser.Mode;
import org.crsh.cmdline.parser.Parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class InvocationMatcher<T> {

  /** . */
  private final CommandDescriptor<T> descriptor;

  /** . */
  private final String mainName;

  public InvocationMatcher(CommandDescriptor<T> descriptor, String mainName) {
    this.descriptor = descriptor;
    this.mainName = mainName;
  }

  public InvocationMatch<T> match(String name, Map<String, ?> options, List<?> arguments) throws SyntaxException {
    class TokenizerImpl extends ArrayList<Token> {
      int last() {
        return size() > 0 ? get(size() - 1).getTo() : 0;
      }
      @Override
      public boolean add(Token token) {
        if (size() > 0) {
          super.add(new Token.Whitespace(last(), " "));
        }
        return super.add(token);
      }

      public void addOption(String name) {
        if (name.length() == 1) {
          add(new Token.Literal.Option.Short(last(), "-" + name));
        } else {
          add(new Token.Literal.Option.Long(last(), "--" + name));
        }
      }
    }
    final TokenizerImpl t = new TokenizerImpl();

    // Add name
    if (name != null && name.length() > 0) {
      t.add(new Token.Literal.Word(t.last(), name));
    }

    // Add options
    for (Map.Entry<String, ?> option : options.entrySet()) {
      if (option.getValue() instanceof Boolean) {
        if ((Boolean)option.getValue()) {
          t.addOption(option.getKey());
        }
      } else {
        t.addOption(option.getKey());
        t.add(new Token.Literal.Word(t.last(), option.getValue().toString()));
      }
    }

    //
    for (Object argument : arguments) {
      t.add(new Token.Literal.Word(t.last(), argument.toString()));
    }

    //
    Tokenizer tokenizer = new Tokenizer() {

      Iterator<Token> i = t.iterator();

      @Override
      protected Token parse() {
        return i.hasNext() ? i.next() : null;
      }

      @Override
      public Delimiter getDelimiter() {
        return Delimiter.EMPTY;
      }
    };

    //
    return match(tokenizer);
  }

  public InvocationMatch<T> match(String s) throws SyntaxException {
    return match(new TokenizerImpl(s));
  }

  private InvocationMatch<T> match(Tokenizer tokenizer) throws SyntaxException {

    //
    Parser<T> parser = new Parser<T>(tokenizer, descriptor, mainName, Mode.INVOKE);
    InvocationMatch<T> current = descriptor.createInvocationMatch();

    //
    while (true) {
      Event event = parser.next();
      if (event instanceof Event.Separator) {
        //
      } else if (event instanceof Event.Stop) {
        while (true) {
          InvocationMatch<T> sub = current.subordinate(mainName);
          if (sub != null) {
            current = sub;
          } else {
            break;
          }
        }
        break;
      } else if (event instanceof Event.Option) {
        Event.Option optionEvent = (Event.Option)event;
        OptionDescriptor desc = optionEvent.getDescriptor();
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
              argumentEvent.getDescriptor(),
              argumentEvent.getFrom(),
              argumentEvent.getTo(),
              bilto(argumentEvent.getValues())
          );
          if (argumentEvent.getDescriptor().getOwner() == current.getDescriptor()) {
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
