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
import org.crsh.lang.spi.Language;
import org.crsh.lang.impl.script.ScriptRepl;
import org.crsh.lang.spi.Repl;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import java.util.LinkedHashMap;
import java.util.Map;

/** @author Julien Viet */
public class repl extends BaseCommand implements ReplCompleter {

  @Usage("list the repl or change the current repl")
  @Command
  public void main(
      InvocationContext<Object> context,
      @Argument(completer = ReplCompleter.class)
      @Usage("the optional repl name")
      String name) throws Exception {
    ShellSession session = (ShellSession)context.getSession();
    Repl current = session.getRepl();
    if (name != null) {
      if (name.equals(current.getLanguage().getName())) {
        context.provide("Using repl " + name);
      } else {
        Repl found = null;
        for (Language lang : session.getContext().getPlugins(Language.class)) {
          if (lang.getName().equals(name)) {
            if (lang.isActive()) {
              found = lang.getRepl();
              if (found != null) {
                break;
              } else {
                throw new ScriptException("Language " + name + " does not provide a repl");
              }
            } else {
              throw new ScriptException("Language " + name + " not active");
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
      LinkedHashMap<Repl, Boolean> repls = new LinkedHashMap<Repl, Boolean>();
      repls.put(ScriptRepl.getInstance(), true);
      for (Language lang : session.getContext().getPlugins(Language.class)) {
        Repl repl = lang.getRepl();
        if (repl != null) {
          repls.put(repl, lang.isActive());
        }
      }

      //
      TableElement table = new TableElement().rightCellPadding(1);
      table.row(
        new LabelElement("NAME").style(Style.style(Decoration.bold)),
        new LabelElement("DISPLAY NAME"),
        new LabelElement("DESCRIPTION"),
        new LabelElement("ACTIVE")
      );
      for (Map.Entry<Repl, Boolean> entry : repls.entrySet()) {
        Boolean active = entry.getValue();
        String langDescription = entry.getKey().getDescription();
        String langDisplayName = entry.getKey().getLanguage().getDisplayName();
        String langName = entry.getKey().getLanguage().getName();
        table.row(
          new LabelElement(langName).style(Style.style(Color.red)),
          new LabelElement(langDisplayName != null ? langDisplayName : ""),
          new LabelElement(langDescription != null ? langDescription : ""),
          new LabelElement(active)
        );
      }

      //
      context.provide(new LabelElement("Current repl is " + current.getLanguage().getName() + "available repl are:\n"));
      context.provide(table);
    }
  }

  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    ShellSession session = (ShellSession)context.getSession();
    Completion.Builder builder = Completion.builder(prefix);
    for (Language lang : session.getContext().getPlugins(Language.class)) {
      if (lang.isActive()) {
        if (lang.getRepl() != null) {
          String name = lang.getName();
          if (name.startsWith(prefix)) {
            builder.add(name.substring(prefix.length()), true);
          }
        }
      }
    }
    return builder.build();
  }
}
