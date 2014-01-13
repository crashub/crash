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

import org.crsh.cli.impl.tokenizer.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Julien Viet
 */
class TokenList implements Iterable<Token> {

  /** . */
  final ArrayList<Token> list = new ArrayList<Token>();

  TokenList() {
  }

  TokenList(Iterable<Token> tokens) {
    for (Token token : tokens) {
      list.add(token);
    }
  }

  int last() {
    return list.size() > 0 ? list.get(list.size() - 1).getTo() : 0;
  }

  public void add(Token token) {
    if (list.size() > 0) {
      list.add(new Token.Whitespace(last(), " "));
    }
    list.add(token);
  }

  public void addOption(String name) {
    if (name.length() == 1) {
      add(new Token.Literal.Option.Short(last(), "-" + name));
    } else {
      add(new Token.Literal.Option.Long(last(), "--" + name));
    }
  }

  public void addOption(String name, List<?> value) {
    if (value.size() > 0) {
      Object first = value.get(0);
      if (first instanceof Boolean) {
        for (Object o : value) {
          if ((Boolean)o) {
            addOption(name);
          }
        }
      } else {
        for (Object o : value) {
          addOption(name);
          add(new Token.Literal.Word(last(), o.toString()));
        }
      }
    }
  }

  @Override
  public Iterator<Token> iterator() {
    return list.iterator();
  }
}
