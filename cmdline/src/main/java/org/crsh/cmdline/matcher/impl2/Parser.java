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
import org.crsh.cmdline.OptionDescriptor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Parser<T> {

  private enum Status {

    READING_OPTION,

    READING_ARG,

    ERROR,

    DONE

  }
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

  /** . */
  private Integer currentArgument;

  public Parser(Tokenizer tokenizer, ClassDescriptor<T> command, String mainName, boolean satisfyAllArguments) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.mainName = mainName;
    this.status = Status.READING_OPTION;
    this.satisfyAllArguments = satisfyAllArguments;
    this.currentArgument = null;
  }

  public boolean isSatisfyAllArguments() {
    return satisfyAllArguments;
  }

  public Event bilto() {

    //
    Event nextEvent = null;
    Status nextStatus = null;

    //
    Token token = tokenizer.peek();
    do {
      if (token == null) {
        nextStatus = Status.DONE;
        nextEvent = new Event.End(Code.DONE);
      } else {
        if (token instanceof Token.Whitespace) {
          nextEvent = new Event.Separator();
          tokenizer.next();
        } else {
          Token.Literal literal = (Token.Literal)token;
          switch (status) {
            case READING_OPTION:
              if (literal.isOption()) {
                OptionDescriptor<?> desc = command.findOption(literal.value);
                if (desc != null) {
                  tokenizer.next();
                  int arity = desc.getArity();
                  LinkedList<String> values = new LinkedList<String>();
                  while (arity > 0) {
                    if (tokenizer.hasNext()) {
                      Token a = tokenizer.peek();
                      if (a instanceof Token.Whitespace) {
                        tokenizer.next();
                      } else {
                        Token.Literal b = (Token.Literal)a;
                        if (b.type == TokenType.WORD) {
                          String value = b.value;
                          values.addLast(value);
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
                  nextEvent = new Event.Option(desc, values);
                } else {
                  // We are reading an unknown option
                  // it could match an option of an implicit command
                  if (command instanceof ClassDescriptor<?>) {
                    MethodDescriptor<T> m = ((ClassDescriptor<T>)command).getMethod(mainName);
                    if (m != null) {
                      desc = m.findOption(literal.value);
                      if (desc != null) {
                        command = m;
                        nextEvent = new Event.Method(m);
                      } else {
                        nextStatus = Status.ERROR;
                        nextEvent = new Event.End(Code.NO_SUCH_METHOD_OPTION);
                      }
                    } else {
                      nextStatus = Status.ERROR;
                      nextEvent = new Event.End(Code.NO_SUCH_CLASS_OPTION);
                    }
                  } else {
                    nextStatus = Status.ERROR;
                    nextEvent = new Event.End(Code.NO_SUCH_METHOD_OPTION);
                  }
                }
              } else {
                if (command instanceof ClassDescriptor<?>) {
                  ClassDescriptor<T> classCommand = (ClassDescriptor<T>)command;
                  MethodDescriptor<T> m = classCommand.getMethod(literal.value);
                  if (m != null) {
                    command = m;
                    tokenizer.next();
                    nextEvent = new Event.Method(m);
                  } else {
                    m = classCommand.getMethod(mainName);
                    if (m != null) {
                      nextEvent = new Event.Method(m);
                      nextStatus = Status.READING_ARG;
                      command = m;
                    } else {
                      nextStatus = Status.ERROR;
                      nextEvent = new Event.End(Code.NO_METHOD);
                    }
                  }
                } else {
                  nextStatus = Status.READING_ARG;
                }
              }
              break;
            case READING_ARG:

              if (satisfyAllArguments) {
                throw new AssertionError("todo");
              } else {
                List<? extends ArgumentDescriptor<?>> arguments = command.getArguments();
                if (arguments.size() > 0) {
                  if (currentArgument == null) {
                    throw new UnsupportedOperationException();
                  }
                } else {
                  nextStatus = Status.ERROR;
                  nextEvent = new Event.End(Code.NO_ARGUMENT);
                }
              }

/*
            LinkedList<Token> remaining = new LinkedList<Token>();
            int count = 0;
            do {
              Token t = tokenizer.next();
              if (t instanceof Token.Literal) {
                count++;
              }
              remaining.add(t);
            }
            while (tokenizer.hasNext());

            //
            List<? extends ArgumentDescriptor<?>> arguments = command.getArguments();

            // First we assign all the mandatory values
            for (ArgumentDescriptor<?> argument : arguments) {
              if (argument.getMultiplicity() == Multiplicity.ONE) {
                count--;
              }
            }


            throw new UnsupportedOperationException("todo");
*/
              break;
            case ERROR:
              throw new UnsupportedOperationException();
            default:
              throw new AssertionError();
          }
        }
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
