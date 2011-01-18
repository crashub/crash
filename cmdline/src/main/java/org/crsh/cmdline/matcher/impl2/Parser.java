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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Parser<T> {

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

  public Event bilto() {

    //
    Event nextEvent = null;

    //
    Token token = tokenizer.peek();
    do {
      Status nextStatus = null;
      if (token instanceof Token.Whitespace) {
      } else {
        final Token.Literal literal = (Token.Literal)token;
      }

      if (status instanceof Status.ReadingOption) {
        if (token == null) {
          nextStatus = new Status.End(Code.DONE);
        } else if (token instanceof Token.Whitespace) {
          nextEvent = new Event.Separator();
          tokenizer.next();
        } else {
          Token.Literal literal = (Token.Literal)token;
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
                  nextStatus = new Status.WantReadArg();
                  command = m;
                } else {
                  nextStatus = new Status.End(Code.NO_METHOD);
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
          nextEvent = new Event.Separator();
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
                nextEvent = new Event.Argument(argument, Arrays.asList(literal.value));
                nextStatus = ra.next();
                break;
              case ZERO_OR_MORE:
                tokenizer.next();
                List<String> values = new ArrayList<String>();
                values.add(literal.value);
                while (tokenizer.hasNext()) {
                  Token capture = tokenizer.next();
                  if (capture instanceof Token.Literal) {
                    values.add(((Token.Literal)capture).value);
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
          nextEvent = new Event.Separator();
          tokenizer.next();
        } else {
          // Create a pattern that represent our arguments
          StringBuilder sb = new StringBuilder("^");
          for (ArgumentDescriptor<?> arg : command.getArguments()) {
            switch (arg.getMultiplicity()) {
              case ZERO_OR_ONE:
                sb.append("(.?)");
                break;
              case ONE:
                sb.append("(.)");
                break;
              case ZERO_OR_MORE:
                sb.append("(.*)");
                break;
            }
          }
          Pattern p = Pattern.compile(sb.toString());

          // Create
          StringBuilder a = new StringBuilder();
          int tokenCount = 0;
          do {
            Token t = tokenizer.next();
            if (t instanceof Token.Literal) {
              a.append("_");
            }
            tokenCount++;
          }
          while (tokenizer.hasNext());
          tokenizer.pushBack(tokenCount);

          //
          LinkedList<Event> events = new LinkedList<Event>();
          Matcher matcher = p.matcher(a);
          if (matcher.find()) {
            for (int i = 1;i <= matcher.groupCount();i++) {
              ArgumentDescriptor<?> argument = command.getArgument(i - 1);
              String group = matcher.group(i);
              int count = group.length();
              List<String> values = new ArrayList<String>(count);
              while (count > 0) {
                Token t = tokenizer.next();
                if (t instanceof Token.Literal) {
                  values.add(((Token.Literal)t).value);
                  count--;
                }
              }
              events.addLast(new Event.Argument(argument, values));
              if (tokenizer.hasNext()) {
                events.addLast(new Event.Separator());
                tokenizer.next();
              }
            }
            nextStatus = new Status.Arg(events, new Status.End(Code.DONE));
          } else {
            throw new UnsupportedOperationException();
          }
        }
      } else if (status instanceof Status.Arg) {
        Status.Arg sa = (Status.Arg)status;
        if (sa.events.isEmpty()) {
          nextStatus = sa.next;
        } else {
          nextEvent = sa.events.removeFirst();
        }
      } else if (status instanceof Status.End) {
        nextEvent = new Event.End(((Status.End)status).code);
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
