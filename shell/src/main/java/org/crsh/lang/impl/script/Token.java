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
package org.crsh.lang.impl.script;

import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.util.Utils;

/**
 * @author Julien Viet
 */
public class Token {

  /** . */
  public final String value;

  /** . */
  public final Token next;

  public Token(String value, Token next) {
    if (value == null) {
      throw new NullPointerException("No null value");
    }
    this.value = value;
    this.next = next;
  }

  public PipeLineFactory createFactory() throws CommandException {
    if (next != null) {
      PipeLineFactory nextFactory = next.createFactory();
      if (nextFactory != null) {
        return new PipeLineFactory(value, nextFactory);
      } else {
        throw new CommandException(ErrorKind.SYNTAX, "");
      }
    } else {
      return Utils.notBlank(value) ? new PipeLineFactory(value, null) : null;
    }
  }

  public Token getLast() {
    return next != null ? next.getLast() : this;
  }

  public static Token parse(CharSequence s) {
    return parse(s, 0);
  }

  public static Token parse(final CharSequence s, final int index) {
    Character lastQuote = null;
    int pos = index;
    while (pos < s.length()) {
      char c = s.charAt(pos);
      if (lastQuote == null) {
        if (c == '|') {
          break;
        } else if (c == '"' || c == '\'') {
          lastQuote = c;
        }
      } else {
        if (lastQuote == c) {
          lastQuote = null;
        }      }
      pos++;
    }
    Token next = pos < s.length() ? parse(s, pos + 1) : null;
    return new Token(s.subSequence(index, pos).toString(), next);
  }
}
