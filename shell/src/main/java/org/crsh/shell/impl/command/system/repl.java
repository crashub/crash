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
package org.crsh.shell.impl.command.system;

import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.lang.script.ScriptREPL;
import org.crsh.repl.REPL;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

import java.io.IOException;
import java.util.ArrayList;

/** @author Julien Viet */
public class repl extends BaseCommand implements ReplCompleter {

  @Usage("list the repl or change the current repl")
  @Command
  public void main(
      InvocationContext<Object> context,
      @Argument(completer = ReplCompleter.class)
      @Usage("the optional repl name")
      String name) throws IOException {
    CRaSHSession session = (CRaSHSession)context.getSession();
    REPL current = session.getRepl();
    if (name != null) {
      if (name.equals(current.getName())) {
        context.provide("Using repl " + name);
      } else {
        REPL found = null;
        if ("script".equals(name)) {
          found = ScriptREPL.getInstance();
        } else {
          for (REPL repl : session.crash.getContext().getPlugins(REPL.class)) {
            if (repl.getName().equals(name)) {
              if (repl.isActive()) {
                found = repl;
                break;
              } else {
                throw new ScriptException("Repl " + name + " is not active");
              }
            }
          }
        }
        if (found != null) {
          session.setRepl(found);
          context.provide("Using repl " + name);
        } else {
          throw new ScriptException("Repl " + name + " not found");
        }
      }
    } else {

      //
      ArrayList<REPL> repls = new ArrayList<REPL>();
      repls.add(ScriptREPL.getInstance());
      for (REPL repl : session.crash.getContext().getPlugins(REPL.class)) {
        repls.add(repl);
      }

      //
      TableElement table = new TableElement().rightCellPadding(1);
      table.add(
          new RowElement().
              add(new LabelElement("NAME").style(Style.style(Decoration.bold))).
              add(new LabelElement("DESCRIPTION")).
              add(new LabelElement("ACTIVE")));
      for (REPL repl : repls) {
        table.add(
            new RowElement().
                add(new LabelElement(repl.getName()).style(Style.style(Color.red))).
                add(new LabelElement(repl.getDescription())).
                add(new LabelElement(repl.isActive())));
      }

      //
      context.provide(new LabelElement("Current repl is \" + current.getName() + \"available repl are:\n"));
      context.provide(table);
    }
  }

  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    CRaSHSession session = (CRaSHSession)context.getSession();
    Completion.Builder builder = Completion.builder(prefix);
    if ("script".startsWith(prefix)) {
      builder.add("script".substring(prefix.length()), true);
    }
    for (REPL repl : session.crash.getContext().getPlugins(REPL.class)) {
      String name = repl.getName();
      if (name.startsWith(prefix)) {
        builder.add(name.substring(prefix.length()), true);
      }
    }
    return builder.build();
  }
}
