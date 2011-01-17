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

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.OptionDescriptor;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Parser<T> {

  private enum Status {

    READING_OPTION,

    READING_ARG,

    ERROR

  }
  /** . */
  private final Tokenizer tokenizer;

  /** . */
  private final String mainName;

  /** . */
  private CommandDescriptor<T, ?> command;

  /** . */
  private Status status;

  public Parser(Tokenizer tokenizer, ClassDescriptor<T> command, String mainName) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.mainName = mainName;
    this.status = Status.READING_OPTION;
  }

  public Event bilto() {

    //
    if (!tokenizer.hasNext()) {
      return null;
    }

    //
    Event nextEvent = null;
    Status nextStatus = null;

    //
    Token token = tokenizer.peek();
    do {
      if (token instanceof Token.Whitespace) {
        nextEvent = new Event.Separator();
      } else {
        Token.Literal literal = (Token.Literal)token;
        switch (status) {
          case READING_OPTION:
            if (literal.isOption()) {
              OptionDescriptor<?> desc = command.getOption(literal.value);
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
                    desc = m.getOption(literal.value);
                    if (desc != null) {
                      command = m;
                      nextEvent = new Event.Method(m);
                    } else {
                      nextStatus = Status.ERROR;
                      nextEvent = new Event.Error();
                    }
                  } else {
                    nextStatus = Status.ERROR;
                    nextEvent = new Event.Error();
                  }
                } else {
                  nextStatus = Status.ERROR;
                  nextEvent = new Event.Error();
                }
              }
            } else {
              if (command instanceof ClassDescriptor<?>) {
                MethodDescriptor<T> m = ((ClassDescriptor<T>)command).getMethod(literal.value);
                if (m != null) {
                  tokenizer.next();
                  nextEvent = new Event.Method(m);
                  nextStatus = Parser.Status.READING_ARG;
                } else {
                  nextStatus = Status.READING_ARG;
                }
              } else {
                nextStatus = Status.READING_ARG;
              }
            }
            break;
          case READING_ARG:
            throw new UnsupportedOperationException("todo");
          default:
            throw new AssertionError();
        }
      }
    }
    while (nextEvent == null);

    //
    this.status = nextStatus;

    //
    return nextEvent;
  }
}
