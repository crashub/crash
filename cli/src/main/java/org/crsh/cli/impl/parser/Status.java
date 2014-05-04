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
import org.crsh.cli.impl.Multiplicity;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.impl.tokenizer.Token;
import org.crsh.cli.impl.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

abstract class Status {

  /**
   * The input.
   */
  static class Request<T> {

    /** . */
    final Mode mode;

    /** . */
    Tokenizer tokenizer;

    /** . */
    final CommandDescriptor<T> command;

    Request(Mode mode, Tokenizer tokenizer, CommandDescriptor<T> command) {
      this.mode = mode;
      this.tokenizer = tokenizer;
      this.command = command;
    }
  }

  /**
   * The output.
   */
  static class Response<T> {

    /** . */
    Status status;

    /** . */
    LinkedList<Event> events;

    /** . */
    CommandDescriptor<T> command;

    Response(Status status) {
      this.status = status;
      this.events = null;
      this.command = null;
    }

    Response() {
      this.status = null;
      this.events = null;
      this.command = null;
    }

    void add(Event event) {
      if (events == null) {
        events = new LinkedList<Event>();
      }
      events.add(event);
    }

    void addAll(Collection<Event> toAdd) {
      if (events == null) {
        events = new LinkedList<Event>();
      }
      events.addAll(toAdd);
    }
  }

  /**
   * Process a request.
   *
   * @param req the request
   * @param <T> the generic type of the command
   * @return the response
   */
  abstract <T> Response<T> process(Request<T> req);

  static class ReadingOption extends Status {

    <T> Response<T> process(Request<T> req) {
      Response<T> response = new Response<T>();
      Token token = req.tokenizer.peek();
      if (token == null) {
        response.add(new Event.Stop.Done(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {
        Token.Literal literal = (Token.Literal)token;
        if (literal instanceof Token.Literal.Option) {
          Token.Literal.Option optionToken = (Token.Literal.Option)literal;
          if (optionToken.getName().length() == 0 && optionToken instanceof Token.Literal.Option.Long) {
            req.tokenizer.next();
            if (req.tokenizer.hasNext()) {
              response.status = new Status.WantReadArg();
            } else {
              if (req.mode == Mode.INVOKE) {
                response.status = new Status.Done();
                response.add(new Event.Stop.Done(req.tokenizer.getIndex()));
              } else {
                response.add(new Event.Stop.Unresolved.NoSuchOption(optionToken));
              }
            }
          } else {
            OptionDescriptor desc = req.command.resolveOption(literal.getValue());
            if (desc != null) {
              req.tokenizer.next();
              int arity = desc.getArity();
              LinkedList<Token.Literal.Word> values = new LinkedList<Token.Literal.Word>();
              while (arity > 0) {
                if (req.tokenizer.hasNext()) {
                  Token a = req.tokenizer.peek();
                  if (a instanceof Token.Whitespace) {
                    req.tokenizer.next();
                    if (req.tokenizer.hasNext() && req.tokenizer.peek() instanceof Token.Literal.Word) {
                      // ok
                    } else {
                      req.tokenizer.pushBack();
                      break;
                    }
                  } else {
                    Token.Literal b = (Token.Literal)a;
                    if (b instanceof Token.Literal.Word) {
                      values.addLast((Token.Literal.Word)b);
                      req.tokenizer.next();
                      arity--;
                    } else {
                      req.tokenizer.pushBack();
                      break;
                    }
                  }
                } else {
                  break;
                }
              }
              response.add(new Event.Option(req.command, desc, optionToken, values));
            } else {
              response.add(new Event.Stop.Unresolved.NoSuchOption(optionToken));
            }
          }
        } else {
          Token.Literal.Word wordLiteral = (Token.Literal.Word)literal;
          CommandDescriptor<T> m = req.command.getSubordinate(wordLiteral.getValue());
          if (m != null) {
            response.command = m;
            req.tokenizer.next();
            response.add(new Event.Subordinate.Explicit(m, wordLiteral));
          } else {
            response.status = new Status.WantReadArg();
          }
        }
      }
      return response;
    }

  }

  static class WantReadArg extends Status {
    @Override
    <T> Response<T> process(Request<T> req) {
      switch (req.mode) {
        case INVOKE:
          return new Response<T>(new Status.ComputeArg());
        case COMPLETE:
          return new Response<T>(new Status.ReadingArg());
        default:
          throw new AssertionError();
      }
    }
  }

  static class ComputeArg extends Status {

    @Override
    <T> Response<T> process(Request<T> req) {
      Token token = req.tokenizer.peek();
      Response<T> response = new Response<T>();
      if (token == null) {
        response.add(new Event.Stop.Done(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {

        //
        List<? extends ArgumentDescriptor> arguments = req.command.getArguments();

        // Count the number ok remaining non whitespace;
        int tokenCount = 0;
        int wordCount = 0;
        do {
          Token t = req.tokenizer.next();
          if (t instanceof Token.Literal) {
            wordCount++;
          }
          tokenCount++;
        }
        while (req.tokenizer.hasNext());
        req.tokenizer.pushBack(tokenCount);

        //
        int oneCount = 0;
        int zeroOrOneCount = 0;
        int index = 0;
        for (ArgumentDescriptor argument : arguments) {
          Multiplicity multiplicity = argument.getMultiplicity();
          if (multiplicity == Multiplicity.SINGLE) {
            if (argument.isRequired()) {
              if (oneCount + 1 > wordCount) {
                break;
              }
              oneCount++;
            } else {
              zeroOrOneCount++;
            }
          }
          index++;
        }

        // This the number of arguments we can satisfy
        arguments = arguments.subList(0, index);

        // How many words we can consume for zeroOrOne and zeroOrMore
        int toConsume = wordCount - oneCount;

        // Correct the zeroOrOneCount and adjust toConsume
        zeroOrOneCount = Math.min(zeroOrOneCount, toConsume);
        toConsume -= zeroOrOneCount;

        // The remaining
        LinkedList<Event> events = new LinkedList<Event>();
        for (ArgumentDescriptor argument : arguments) {
          int size;
          switch (argument.getMultiplicity()) {
            case SINGLE:
              if (argument.isRequired()) {
                size = 1;
              } else {
                if (zeroOrOneCount > 0) {
                  zeroOrOneCount--;
                  size = 1;
                } else {
                  size = 0;
                }
              }
              break;
            case MULTI:
              // We consume the remaining
              size = toConsume;
              toConsume = 0;
              break;
            default:
              throw new AssertionError();
          }

          // Now take care of the argument
          if (size > 0) {
            List<Token.Literal> values = new ArrayList<Token.Literal>(size);
            while (size > 0) {
              Token t = req.tokenizer.next();
              if (t instanceof Token.Literal) {
                values.add(((Token.Literal)t));
                size--;
              }
            }
            events.addLast(new Event.Argument(req.command, argument, values));

            // Add the whitespace if needed
            if (req.tokenizer.hasNext() && req.tokenizer.peek() instanceof Token.Whitespace) {
              events.addLast(new Event.Separator((Token.Whitespace) req.tokenizer.next()));
            }
          }
        }

        //
        events.addLast(new Event.Stop.Done(req.tokenizer.getIndex()));

        //
        response.status = new Status.Done();
        response.addAll(events);
      }
      return response;
    }
  }

  static class Done extends Status {
    @Override
    <T> Response<T> process(Request<T> req) {
      throw new IllegalStateException();
    }
  }

  static class ReadingArg extends Status {

    /** . */
    private final int index;

    ReadingArg() {
      this(0);
    }

    private ReadingArg(int index) {
      this.index = index;
    }

    ReadingArg next() {
      return new ReadingArg(index + 1);
    }

    @Override
    <T> Response<T> process(Request<T> req) {
      Token token = req.tokenizer.peek();
      Response<T> response = new Response<T>();
      if (token == null) {
        response.add(new Event.Stop.Done(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {
        final Token.Literal literal = (Token.Literal)token;
        List<? extends ArgumentDescriptor> arguments = req.command.getArguments();
        if (index < arguments.size()) {
          ArgumentDescriptor argument = arguments.get(index);
          switch (argument.getMultiplicity()) {
            case SINGLE:
              req.tokenizer.next();
              response.add(new Event.Argument(req.command, argument, Arrays.asList(literal)));
              response.status = next();
              break;
            case MULTI:
              req.tokenizer.next();
              List<Token.Literal> values = new ArrayList<Token.Literal>();
              values.add(literal);
              while (req.tokenizer.hasNext()) {
                Token capture = req.tokenizer.next();
                if (capture instanceof Token.Literal) {
                  values.add(((Token.Literal)capture));
                } else {
                  if (req.tokenizer.hasNext()) {
                    // Ok
                  } else {
                    req.tokenizer.pushBack();
                    break;
                  }
                }
              }
              response.add(new Event.Argument(req.command, argument, values));
          }
        } else {
          response.add(new Event.Stop.Unresolved.TooManyArguments(literal));
        }
      }
      return response;
    }
  }
}
