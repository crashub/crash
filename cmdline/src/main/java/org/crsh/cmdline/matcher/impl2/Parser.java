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

import org.crsh.cmdline.ArgumentDescriptor;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Parser<T> {

  /** . */
  private final Tokenizer tokenizer;

  /** . */
  private final String mainName;

  /** . */
  private final boolean satisfyAllArguments;

  /** . */
  private CommandDescriptor<T, ?> command;

  /** . */
  private Status status;

  public Parser(Tokenizer tokenizer, ClassDescriptor<T> command, String mainName, boolean satisfyAllArguments) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.mainName = mainName;
    this.status = new Status.ReadingOption();
    this.satisfyAllArguments = satisfyAllArguments;
  }

  public boolean isSatisfyAllArguments() {
    return satisfyAllArguments;
  }

  public int getIndex() {
    return tokenizer.getIndex();
  }

  public Event bilto() {

    //
    Event nextEvent = null;

    //
    Token token = tokenizer.peek();
    do {
      Status nextStatus = null;
      if (status instanceof Status.ReadingOption) {
        if (token == null) {
          nextStatus = new Status.End(Code.DONE);
        } else if (token instanceof Token.Whitespace) {
          nextEvent = new Event.Separator((Token.Whitespace)token);
          tokenizer.next();
        } else {
          Token.Literal literal = (Token.Literal)token;
          if (literal instanceof Token.Literal.Option) {
            Token.Literal.Option optionToken = (Token.Literal.Option)literal;
            OptionDescriptor<?> desc = command.findOption(literal.value);
            if (desc != null) {
              tokenizer.next();
              int arity = desc.getArity();
              LinkedList<Token.Literal.Word> values = new LinkedList<Token.Literal.Word>();
              while (arity > 0) {
                if (tokenizer.hasNext()) {
                  Token a = tokenizer.peek();
                  if (a instanceof Token.Whitespace) {
                    tokenizer.next();
                  } else {
                    Token.Literal b = (Token.Literal)a;
                    if (b instanceof Token.Literal.Word) {
                      values.addLast((Token.Literal.Word)b);
                      tokenizer.next();
                      arity--;
                    } else {
                      tokenizer.pushBack();
                      break;
                    }
                  }
                } else {
                  break;
                }
              }
              nextEvent = new Event.Option(desc, optionToken, values);
            } else {
              // We are reading an unknown option
              // it could match an option of an implicit command
              if (command instanceof ClassDescriptor<?>) {
                MethodDescriptor<T> m = ((ClassDescriptor<T>)command).getMethod(mainName);
                if (m != null) {
                  desc = m.findOption(literal.value);
                  if (desc != null) {
                    command = m;
                    nextEvent = new Event.Method.Implicit(m, literal);
                  } else {
                    nextStatus = new Status.End(Code.NO_SUCH_METHOD_OPTION);
                  }
                } else {
                  nextStatus = new Status.End(Code.NO_SUCH_CLASS_OPTION);
                }
              } else {
                nextStatus = new Status.End(Code.NO_SUCH_METHOD_OPTION);
              }
            }
          } else {
            Token.Literal.Word wordLiteral = (Token.Literal.Word)literal;
            if (command instanceof ClassDescriptor<?>) {
              ClassDescriptor<T> classCommand = (ClassDescriptor<T>)command;
              MethodDescriptor<T> m = classCommand.getMethod(wordLiteral.value);
              if (m != null) {
                command = m;
                tokenizer.next();
                nextEvent = new Event.Method.Explicit(m, wordLiteral);
              } else {
                m = classCommand.getMethod(mainName);
                if (m != null) {
                  nextEvent = new Event.Method.Implicit(m, wordLiteral);
                  nextStatus = new Status.WantReadArg();
                  command = m;
                } else {
                  nextStatus = new Status.WantReadArg();
                }
              }
            } else {
              nextStatus = new Status.WantReadArg();
            }
          }
        }
      } else if (status instanceof Status.WantReadArg) {
        if (satisfyAllArguments) {
          nextStatus = new Status.ComputeArg();
        } else {
          nextStatus = new Status.ReadingArg();
        }
      } else if (status instanceof Status.ReadingArg) {
        if (token == null) {
          nextStatus = new Status.End(Code.DONE);
        } else if (token instanceof Token.Whitespace) {
          nextEvent = new Event.Separator((Token.Whitespace)token);
          tokenizer.next();
        } else {
          final Token.Literal literal = (Token.Literal)token;
          List<? extends ArgumentDescriptor<?>> arguments = command.getArguments();
          Status.ReadingArg ra = (Status.ReadingArg)status;
          if (ra.index < arguments.size()) {
            ArgumentDescriptor<?> argument = arguments.get(ra.index);
            switch (argument.getMultiplicity()) {
              case ZERO_OR_ONE:
              case ONE:
                tokenizer.next();
                nextEvent = new Event.Argument(argument, Arrays.asList(literal));
                nextStatus = ra.next();
                break;
              case ZERO_OR_MORE:
                tokenizer.next();
                List<Token.Literal> values = new ArrayList<Token.Literal>();
                values.add(literal);
                while (tokenizer.hasNext()) {
                  Token capture = tokenizer.next();
                  if (capture instanceof Token.Literal) {
                    values.add(((Token.Literal)capture));
                  } else {
                    if (tokenizer.hasNext()) {
                      // Ok
                    } else {
                      tokenizer.pushBack();
                      break;
                    }
                  }
                }
                nextEvent = new Event.Argument(argument, values);
            }
          } else {
            nextStatus = new Status.End(Code.NO_ARGUMENT);
          }
        }
      } else if (status instanceof Status.ComputeArg) {
        if (token == null) {
          nextStatus = new Status.End(Code.DONE);
        } else if (token instanceof Token.Whitespace) {
          nextEvent = new Event.Separator((Token.Whitespace)token);
          tokenizer.next();
        } else {

          //
          List<? extends ArgumentDescriptor<?>> arguments = command.getArguments();

          // Count the number ok remaining non whitespace;
          int tokenCount = 0;
          int wordCount = 0;
          do {
            Token t = tokenizer.next();
            if (t instanceof Token.Literal) {
              wordCount++;
            }
            tokenCount++;
          }
          while (tokenizer.hasNext());
          tokenizer.pushBack(tokenCount);

          //
          int oneCount = 0;
          int zeroOrOneCount = 0;
          int index = 0;
          for (ArgumentDescriptor<?> argument : arguments) {
            Multiplicity multiplicity = argument.getMultiplicity();
            if (multiplicity == Multiplicity.ONE) {
              if (oneCount + 1 > wordCount) {
                break;
              }
              oneCount++;
            } else if (multiplicity == Multiplicity.ZERO_OR_ONE) {
              zeroOrOneCount++;
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
          for (ArgumentDescriptor<?> argument : arguments) {
            int size;
            switch (argument.getMultiplicity()) {
              case ONE:
                size = 1;
                break;
              case ZERO_OR_ONE:
                if (zeroOrOneCount > 0) {
                  zeroOrOneCount--;
                  size = 1;
                } else {
                  size = 0;
                }
                break;
              case ZERO_OR_MORE:
                // We consume the remaining
                size = toConsume;
                toConsume = 0;
                break;
              default:
                throw new AssertionError();
            }

            // Now take care of the size found
            List<Token.Literal> values = new ArrayList<Token.Literal>(size);
            while (size > 0) {
              Token t = tokenizer.next();
              if (t instanceof Token.Literal) {
                values.add(((Token.Literal)t));
                size--;
              }
            }
            events.addLast(new Event.Argument(argument, values));

            // Add the whitespace if needed
            if (tokenizer.hasNext() && tokenizer.peek() instanceof Token.Whitespace) {
              events.addLast(new Event.Separator((Token.Whitespace)tokenizer.next()));
            }
          }

          //
          nextStatus = new Status.Arg(events, new Status.End(Code.DONE));
        }
      } else if (status instanceof Status.Arg) {
        Status.Arg sa = (Status.Arg)status;
        if (sa.events.isEmpty()) {
          nextStatus = sa.next;
        } else {
          nextEvent = sa.events.removeFirst();
        }
      } else if (status instanceof Status.End) {
        if (token != null) {
          nextEvent = new Event.End(((Status.End)status).code, token.getFrom());
        } else {
          nextEvent = new Event.End(((Status.End)status).code, tokenizer.getIndex());
        }
      } else {
        throw new AssertionError();
      }

      //
      if (nextStatus != null) {
        this.status = nextStatus;
      }
    }
    while (nextEvent == null);

    //
    return nextEvent;
  }
}
