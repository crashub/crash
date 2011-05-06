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

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CRaSHProcessFactory {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final CRaSH crash;

  /** . */
  private final String request;

  CRaSHProcessFactory(CRaSH crash, String request) {
    this.crash = crash;
    this.request = request;
  }

  private static class SimpleProcess extends CRaSHProcess {

    /** . */
    private final ShellResponse response;

    private SimpleProcess(CRaSH crash, String request, ShellResponse response) {
      super(crash, request);
      this.response = response;
    }

    @Override
    ShellResponse invoke(ShellProcessContext context) {
      return response;
    }
  }

  CRaSHProcess create() {

    //
    if ("bye".equals(request)) {
      return new SimpleProcess(crash, request, new ShellResponse.Close());
    } else {

      // Create AST
      Parser parser = new Parser(request);
      AST ast = parser.parse();

      //
      if (ast instanceof AST.Expr) {
        AST.Expr expr = (AST.Expr)ast;

        // Create commands first
        try {
          return expr.create(crash, request);
        } catch (CreateCommandException e) {
          return new SimpleProcess(crash, request, e.getResponse());
        }
      } else {
        return new SimpleProcess(crash, request, new ShellResponse.NoCommand());
      }
    }
  }
}
