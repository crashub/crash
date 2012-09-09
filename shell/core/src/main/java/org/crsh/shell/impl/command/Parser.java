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

package org.crsh.shell.impl.command;

import org.crsh.command.SyntaxException;

class Parser {

  /** . */
  private Tokenizer tokenizer;

  /** . */
  private Token token;

  public Parser(CharSequence s) {
    this.tokenizer = new Tokenizer(s);
    this.token = tokenizer.nextToken();
  }


  /*

  grammar

  expr -> term | term "|" expr
  term -> cmd | cmd "+" term

   */

  public AST parse() {
    if (token == Token.EOF) {
      return null;
    } else {
      return parseExpr();
    }
  }

  private AST.Expr parseExpr() {
    AST.Term term = parseTerm();
    if (token == Token.EOF) {
      return new AST.Expr(term);
    } else if (token == Token.PIPE) {
      token = tokenizer.nextToken();
      AST.Expr next = parseExpr();
      return new AST.Expr(term, next);
    } else {
      throw new SyntaxException("Syntax error");
    }
  }

  private AST.Term parseTerm() {
    if (token instanceof Token.Command) {
      Token.Command command = (Token.Command)token;
      token = tokenizer.nextToken();
      if (token == Token.PLUS) {
        token = tokenizer.nextToken();
        AST.Term next = parseTerm();
        return new AST.Term(command.line, next);
      } else {
        return new AST.Term(command.line);
      }
    } else {
      throw new SyntaxException("Syntax error");
    }
  }
}
