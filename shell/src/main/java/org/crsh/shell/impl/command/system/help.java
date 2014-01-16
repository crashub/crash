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

import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

import java.io.IOException;

/** @author Julien Viet */
public class help extends BaseCommand {

  @Usage("provides basic help")
  @Command
  public void main(InvocationContext<Object> context) throws IOException {

    //
    TableElement table = new TableElement().rightCellPadding(1);
    table.add(
        new RowElement().
            add(new LabelElement("NAME").style(Style.style(Decoration.bold))).
            add(new LabelElement("DESCRIPTION")));

    //
    CRaSH crash = (CRaSH)context.getSession().get("crash");
    Iterable<String> names = crash.getCommandNames();
    for (String name : names) {
      try {
        String desc = crash.getCommandDescription(name);
        if (desc == null) {
          desc = "";
        }
        table.add(
            new RowElement().
                add(new LabelElement(name).style(Style.style(Color.red))).
                add(new LabelElement(desc)));
      } catch (Exception ignore) {
        //
      }
    }

    //
    context.provide(new LabelElement("Try one of these commands with the -h or --help switch:\n"));
    context.provide(table);
  }
}
