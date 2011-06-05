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
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.Multiplicity;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.matcher.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Parser<T> implements Iterator<Event> {

  private static abstract class Status {

    private static class ReadingOption extends Status { }
    private static class WantReadArg extends Status { }
    private static class ComputeArg extends Status { }
    private static class Done extends Status { }
    private static class ReadingArg extends Status {

      /** . */
      final int index;

      ReadingArg() {
        this(0);
      }

      private ReadingArg(int index) {
        this.index = index;
      }

      ReadingArg next() {
        return new ReadingArg(index + 1);
      }
    }
  }

  public enum Mode {

    INVOKE,

    COMPLETE

  }

  /** . */
  private final Tokenizer tokenizer;

  /** . */
  private final String mainName;

  /** . */
  private final Mode mode;

  /** . */
  private CommandDescriptor<T, ?> command;

  /** . */
  private Status status;

  /** . */
  private final LinkedList<Event> next;

  public Parser(Tokenizer tokenizer, ClassDescriptor<T> command, String mainName, Mode mode) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.mainName = mainName;
    this.status = new Status.ReadingOption();
    this.mode = mode;
    this.next = new LinkedList<Event>();
  }

  public Mode getMode() {
    return mode;
  }

  public int getIndex() {
    return tokenizer.getIndex();
  }

  public Status getStatus() {
    return status;
  }

  public boolean hasNext() {
    if (next.isEmpty()) {
      determine();
    }
    return next.size() > 0;
  }

  public Event next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return next.removeFirst();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void determine() {

    //
    Token token = tokenizer.peek();
    while (next.isEmpty()) {
      Status nextStatus = null;
      if (status instanceof Status.ReadingOption) {
        if (token == null) {
          next.addLast(new Event.Stop.Done.Option(tokenizer.getIndex()));
        } else if (token instanceof Token.Whitespace) {
          next.addLast(new Event.Separator((Token.Whitespace)token));
          tokenizer.next();
        } else {
          Token.Literal literal = (Token.Literal)token;
          if (literal instanceof Token.Literal.Option) {
            Token.Literal.Option optionToken = (Token.Literal.Option)literal;
            if (optionToken.getName().length() == 0 && optionToken instanceof Token.Literal.Option.Long) {
              tokenizer.next();
              next.addLast(new Event.DoubleDash((Token.Literal.Option.Long)optionToken));
              if (command instanceof ClassDescriptor<?>) {
                ClassDescriptor<T> classCommand = (ClassDescriptor<T>)command;
                MethodDescriptor<T> m = classCommand.getMethod(mainName);
                if (m != null) {
                  command = m;
                  next.addLast(new Event.Method.Implicit(m, optionToken));
                }
              }
              nextStatus = new Status.WantReadArg();
            } else {
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
                      if (tokenizer.hasNext() && tokenizer.peek() instanceof Token.Literal.Word) {
                        // ok
                      } else {
                        tokenizer.pushBack();
                        break;
                      }
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
                next.addLast(new Event.Option(desc, optionToken, values));
              } else {
                // We are reading an unknown option
                // it could match an option of an implicit command
                if (command instanceof ClassDescriptor<?>) {
                  MethodDescriptor<T> m = ((ClassDescriptor<T>)command).getMethod(mainName);
                  if (m != null) {
                    desc = m.findOption(literal.value);
                    if (desc != null) {
                      command = m;
                      next.addLast(new Event.Method.Implicit(m, literal));
                    } else {
                      next.addLast(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
                    }
                  } else {
                    next.addLast(new Event.Stop.Unresolved.NoSuchOption.Class(optionToken));
                  }
                } else {
                  next.addLast(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
                }
              }
            }
          } else {
            Token.Literal.Word wordLiteral = (Token.Literal.Word)literal;
            if (command instanceof ClassDescriptor<?>) {
              ClassDescriptor<T> classCommand = (ClassDescriptor<T>)command;
              MethodDescriptor<T> m = classCommand.getMethod(wordLiteral.value);
              if (m != null && !m.getName().equals(mainName)) {
                command = m;
                tokenizer.next();
                next.addLast(new Event.Method.Explicit(m, wordLiteral));
              } else {
                m = classCommand.getMethod(mainName);
                if (m != null) {
                  next.addLast(new Event.Method.Implicit(m, wordLiteral));
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
        switch (mode) {
          case INVOKE:
            nextStatus = new Status.ComputeArg();
            break;
          case COMPLETE:
            nextStatus = new Status.ReadingArg();
            break;
          default:
            throw new AssertionError();
        }
      } else if (status instanceof Status.ReadingArg) {
        if (token == null) {
          next.addLast(new Event.Stop.Done.Arg(tokenizer.getIndex()));
        } else if (token instanceof Token.Whitespace) {
          next.addLast(new Event.Separator((Token.Whitespace)token));
          tokenizer.next();
        } else {
          final Token.Literal literal = (Token.Literal)token;
          List<? extends ArgumentDescriptor<?>> arguments = command.getArguments();
          Status.ReadingArg ra = (Status.ReadingArg)status;
          if (ra.index < arguments.size()) {
            ArgumentDescriptor<?> argument = arguments.get(ra.index);
            switch (argument.getMultiplicity()) {
              case SINGLE:
                tokenizer.next();
                next.addLast(new Event.Argument(argument, Arrays.asList(literal)));
                nextStatus = ra.next();
                break;
              case MULTI:
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
                next.addLast(new Event.Argument(argument, values));
            }
          } else {
            next.addLast(new Event.Stop.Unresolved.TooManyArguments(literal));
          }
        }
      } else if (status instanceof Status.ComputeArg) {
        if (token == null) {
          next.addLast(new Event.Stop.Done.Arg(tokenizer.getIndex()));
        } else if (token instanceof Token.Whitespace) {
          next.addLast(new Event.Separator((Token.Whitespace)token));
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
          for (ArgumentDescriptor<?> argument : arguments) {
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
          }

          //
          events.addLast(new Event.Stop.Done.Arg(tokenizer.getIndex()));

          //
          nextStatus = new Status.Done();
          next.addAll(events);
        }
      } else if (status instanceof Status.Done) {
        throw new IllegalStateException();
      } else {
        throw new AssertionError();
      }

      //
      if (nextStatus != null) {
        this.status = nextStatus;
      }
    }
  }
}
