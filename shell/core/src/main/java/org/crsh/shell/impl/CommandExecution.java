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

package org.crsh.shell.impl;

import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CommandExecution implements ShellProcess {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final CRaSH crash;

  /** . */
  private final String request;

  /** . */
  private final ShellProcessContext context;

  CommandExecution(CRaSH crash, String request, ShellProcessContext context) {
    this.crash = crash;
    this.request = request;
    this.context = context;
  }

  void execute() {

    //
    context.begin(this);

    //
    ShellResponse resp;
    if ("bye".equals(request)) {
      resp = new ShellResponse.Close();
    } else {
      // Create AST
      Parser parser = new Parser(request);
      AST ast = parser.parse();

      //
      if (ast instanceof AST.Expr) {
        AST.Expr expr = (AST.Expr)ast;

        // Create commands first
        try {
          resp = expr.createCommands(crash);
        } catch (Exception e) {
          resp = new ShellResponse.Error(ErrorType.EVALUATION, e);
        }

        if (resp == null) {
          resp = expr.execute(context, crash.attributes);
        }
      } else {
        resp = new ShellResponse.NoCommand();
      }
    }

    //
    if (resp instanceof ShellResponse.Error) {
      ShellResponse.Error error = (ShellResponse.Error)resp;
      Throwable t = error.getThrowable();
      if (t != null) {
        log.error("Error while evaluating request '" + request + "' " + error.getText(), t);
      } else {
        log.error("Error while evaluating request '" + request + "' " + error.getText());
      }
    }

    //
    context.end(resp);
  }

  public void cancel() {
    // No op for now
  }
}
