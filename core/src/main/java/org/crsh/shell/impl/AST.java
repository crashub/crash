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

import org.crsh.command.ShellCommand;
import org.crsh.shell.ErrorType;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class AST {

  abstract void createCommands(CRaSH crash);

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

    @Override
    void createCommands(CRaSH crash) {
      term.createCommands(crash);
      if (next != null) {
        next.createCommands(crash);
      }
    }

    ShellResponse execute(ShellResponseContext responseContext, Map<String,Object> attributes, ArrayList consumed) {

      //
      if (term.command == null) {
        return new ShellResponse.UnkownCommand(term.commandDefinition.get(0));
      }

      // What will be produced by this expression
      ArrayList produced = new ArrayList();

      // (need to find better than that)
      ShellResponse response = new ShellResponse.NoCommand();

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

        // Execute command
        try {
          current.command.execute(ctx, current.args);
        } catch (Throwable t) {
          return new ShellResponse.Error(ErrorType.EVALUATION, t);
        }

        //
        if (ctx.getBuffer() != null) {
          response = new ShellResponse.Display(ctx.getBuffer().toString());
        } else {
          response = new ShellResponse.Ok();
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
    private ShellCommand command;

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

    @Override
    void createCommands(CRaSH crash) {
      ShellCommand command = crash.getCommand(commandDefinition.get(0));
      String[] args = new String[commandDefinition.size() - 1];
      commandDefinition.subList(1, commandDefinition.size()).toArray(args);

      //
      this.args = args;
      this.command = command;

      if (next != null) {
        next.createCommands(crash);
      }
    }
  }
}
