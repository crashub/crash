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

import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellProcessContext;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class AST {

  abstract Term lastTerm();

  static class Expr extends AST {

    /** . */
    final Term term;

    /** . */
    final Expr next;

    Expr(Term term) {
      this.term = term;
      this.next = null;
    }

    Expr(Term term, Expr next) {
      this.term = term;
      this.next = next;
    }

    final CRaSHProcess create(CRaSHSession crash, String request) throws CreateCommandException {
      term.create(crash);
      if (next != null) {
        next.create(crash);
      }
      return new CRaSHProcess(crash, request) {
        @Override
        ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {
          return Expr.this.execute(crash, context, null);
        }
      };
    }

    private void create(CRaSHSession crash) throws CreateCommandException {
      term.create(crash);
      if (next != null) {
        next.create(crash);
      }
    }

    protected ShellResponse execute(CRaSHSession crash, ShellProcessContext context, ArrayList consumed) throws InterruptedException {

      // What will be produced by this expression
      ArrayList produced = new ArrayList();

      //
      StringBuilder out = new StringBuilder();

      // Iterate over all terms
      for (Term current = term;current != null;current = current.next) {

        // Build command context
        InvocationContextImpl ctx;
        if (current.invoker.getConsumedType() == Void.class) {
          ctx = new InvocationContextImpl(context, null, crash.attributes);
        } else {
          // For now we assume we have compatible consumed/produced types
          ctx = new InvocationContextImpl(context, consumed, crash.attributes);
        }

        //
        try {
          current.invoker.invoke(ctx);
        } catch (InterruptedException e) {
          throw e;
        } catch (Throwable t) {
          return ShellResponse.internalError(t);
        }

        // Append anything that was in the buffer
        if (ctx.getBuffer() != null) {
          out.append(ctx.getBuffer().toString());
        }

        // Append produced if possible
        if (current.invoker.getProducedType() == Void.class) {
          // Do nothing
        } else {
          produced.addAll(ctx.getProducedItems());
        }
      }

      //
      if (next != null) {
        return next.execute(crash, context, produced);
      } else {
        ShellResponse response;
        if (out.length() > 0) {
          response = ShellResponse.display(produced, out.toString());
        } else {
          response = ShellResponse.ok(produced);
        }
        return response;
      }
    }

    @Override
    Term lastTerm() {
      if (next != null) {
        return next.lastTerm();
      }
      if (term != null) {
        return term.lastTerm();
      }
      return null;
    }
  }

  static class Term extends AST {

    /** . */
    final String line;

    /** . */
    final Term next;

    /** . */
    final String name;

    /** . */
    final String rest;

    /** . */
    private ShellCommand command;

    /** . */
    private CommandInvoker invoker;

    Term(String line) {
      this(line, null);
    }

    Term(String line, Term next) {

      Pattern p = Pattern.compile("^\\s*(\\S+)");
      java.util.regex.Matcher m = p.matcher(line);
      String name = null;
      String rest = null;
      if (m.find()) {
        name = m.group(1);
        rest = line.substring(m.end());
      }

      //
      this.name = name;
      this.rest = rest;
      this.line = line;
      this.next = next;
    }

    private void create(CRaSHSession session) throws CreateCommandException {
      CommandInvoker invoker = null;
      if (name != null) {
        command = session.crash.getCommand(name);
        if (command != null) {
          invoker = command.createInvoker(rest);
        }
      }

      //
      if (invoker == null) {
        throw new CreateCommandException(ShellResponse.unknownCommand(name));
      } else {
        this.invoker = invoker;
      }

      //
      if (next != null) {
        next.create(session);
      }
    }

    String getLine() {
      return line;
    }

    @Override
    Term lastTerm() {
      if (next != null) {
        return next.lastTerm();
      } else {
        return this;
      }
    }
  }
}
