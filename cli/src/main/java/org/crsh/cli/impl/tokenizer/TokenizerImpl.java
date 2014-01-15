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

package org.crsh.cli.impl.tokenizer;

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.line.LineParser;

public class TokenizerImpl extends Tokenizer {

  /** . */
  private final Automaton automaton;

  /** . */
  private Delimiter endingDelimiter;

  public TokenizerImpl(CharSequence s) {

    this.endingDelimiter = Delimiter.EMPTY;

    Automaton automaton = new Automaton(s);
    LineParser.Visitor parser2 = new LineParser.Visitor() {
      public void openStrongQuote(int index) { endingDelimiter = Delimiter.SINGLE_QUOTE; }
      public void closeStrongQuote(int index) { endingDelimiter = Delimiter.EMPTY; }
      public void openWeakQuote(int index) { endingDelimiter =  Delimiter.DOUBLE_QUOTE; }
      public void closeWeakQuote(int index) { endingDelimiter = Delimiter.EMPTY; }
    };

    //
    LineParser parser = new LineParser(automaton, parser2);
    parser.append(s);
    automaton.close();

    //
    this.automaton = automaton;
  }

  protected Token parse() {
    if (automaton.tokens.size() > 0) {
      return automaton.tokens.removeFirst();
    } else {
      return null;
    }
  }

  public Delimiter getEndingDelimiter() {
    return endingDelimiter;
  }
}
