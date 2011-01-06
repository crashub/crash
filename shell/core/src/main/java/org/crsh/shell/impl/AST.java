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
import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellProcessContext;

import java.util.ArrayList;
import java.util.Map;
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

    ShellResponse createCommands(CRaSH crash) {
      ShellResponse resp = term.createCommands(crash);
      if (resp == null) {
        if (next != null) {
          return next.createCommands(crash);
        }
      }
      return resp;
    }

    ShellResponse execute(ShellProcessContext responseContext, Map<String,Object> attributes) {

//      (need to find better than that)
//      ShellResponse response = new ShellResponse.NoCommand();

      //
      try {
        return execute(responseContext, attributes, null);
      } catch (Throwable t) {
        return new ShellResponse.Error(ErrorType.EVALUATION, t);
      }
    }

    private ShellResponse execute(
        ShellProcessContext responseContext,
        Map<String,Object> attributes,
        ArrayList consumed) {

      // What will be produced by this expression
      ArrayList produced = new ArrayList();

      //
      StringBuilder out = new StringBuilder();

      //
      for (Term current = term;current != null;current = current.next) {

        // Build command context
        InvocationContextImpl ctx;
        if (current.invoker.getConsumedType() == Void.class) {
          ctx = new InvocationContextImpl(responseContext, null, attributes);
        } else {
          // For now we assume we have compatible consumed/produced types
          ctx = new InvocationContextImpl(responseContext, consumed, attributes);
        }

        // Do something usefull with command
/*
        String[] args = current.args;
        if (args.length > 0 && ("-h".equals(args[args.length - 1]) || "--help".equals(args[args.length - 1]))) {
          String s = current.command.describe(current.line, DescriptionMode.USAGE);
          if (s != null) {
            ctx.getWriter().print(s);
          }
        } else {
        }
*/
        current.invoker.invoke(ctx);

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
        return next.execute(responseContext, attributes, produced);
      } else {
        ShellResponse response;
        if (out.length() > 0) {
          response = new ShellResponse.Display(produced, out.toString());
        } else {
          response = new ShellResponse.Ok(produced);
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
    private ShellCommand command;

    /** . */
    private CommandInvoker invoker;

    Term(String line, Term next) {
      this.line = line;
      this.next = next;
    }

    Term(String line) {
      this.line = line;
      this.next = null;
    }

    private ShellResponse createCommands(CRaSH crash) {
      CommandInvoker invoker = null;
      Pattern p = Pattern.compile("^\\s*(\\S+)");
      java.util.regex.Matcher m = p.matcher(line);
      String name = null;
      if (m.find()) {
        name = m.group(1);
        try {
          command = crash.getCommand(name);
          if (command != null) {
            invoker = command.createInvoker(line.substring(m.end()));
          }
        }
        catch (CreateCommandException e) {
          crash.log.error("Could not create command " + name, e);
          return new ShellResponse.Error(ErrorType.INTERNAL, e);
        }
      }

      //
      if (invoker == null) {
        return new ShellResponse.UnknownCommand(name);
      }

      //
      this.invoker = invoker;

      //
      if (next != null) {
        return next.createCommands(crash);
      } else {
        return null;
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
