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
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class AST {

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

    ShellResponse.UnknownCommand createCommands(CRaSH crash) {
      ShellResponse.UnknownCommand resp = term.createCommands(crash);
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
        CommandContextImpl ctx;
        if (current.command.getConsumedType() == Void.class) {
          ctx = new CommandContextImpl(responseContext, null, attributes);
        } else {
          // For now we assume we have compatible consumed/produced types
          ctx = new CommandContextImpl(responseContext, consumed, attributes);
        }

        // Do something usefull with command
        String[] args = current.args;
        if (args.length > 0 && ("-h".equals(args[args.length - 1]) || "--help".equals(args[args.length - 1]))) {
          current.command.usage(ctx.getWriter());
        } else {
          current.command.invoke(ctx);
        }

        // Append anything that was in the buffer
        if (ctx.getBuffer() != null) {
          out.append(ctx.getBuffer().toString());
        }

        // Append produced if possible
        if (current.command.getProducedType() == Void.class) {
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
  }

  static class Term extends AST {

    /** . */
    final List<String> commandDefinition;

    /** . */
    final Term next;

    /** . */
    private CommandInvoker command;

    /** . */
    private String[] args;

    Term(List<String> commandDefinition, Term next) {
      this.commandDefinition = commandDefinition;
      this.next = next;
    }

    Term(List<String> commandDefinition) {
      this.commandDefinition = commandDefinition;
      this.next = null;
    }

    private ShellResponse.UnknownCommand createCommands(CRaSH crash) {
      String[] args = new String[commandDefinition.size() - 1];
      commandDefinition.subList(1, commandDefinition.size()).toArray(args);
      String name = commandDefinition.get(0);

      //
      CommandInvoker invoker = null;
      ShellCommand command = crash.getCommand(name);
      if (command != null) {
        invoker = command.createInvoker(args);
      }

      //
      if (invoker == null) {
        return new ShellResponse.UnknownCommand(name);
      }

      //
      this.args = args;
      this.command = invoker;

      //
      if (next != null) {
        return next.createCommands(crash);
      } else {
        return null;
      }
    }
  }
}
