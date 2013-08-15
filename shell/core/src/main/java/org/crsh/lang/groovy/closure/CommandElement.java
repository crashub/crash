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
package org.crsh.lang.groovy.closure;

import org.crsh.command.CommandCreationException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author Julien Viet */
class CommandElement extends PipeLineElement {

  /** . */
  final String commandName;

  /** . */
  final ShellCommand command;

  /** . */
  final String name;

  /** . */
  final Map<String, Object> options;

  /** . */
  final List<Object> args;

  public CommandElement(String commandName, ShellCommand command, String name) {
    this.commandName = commandName;
    this.command = command;
    this.name = name;
    this.options = null;
    this.args = null;
  }

  public CommandElement(String commandName, ShellCommand command, String name, Map<String, Object> options, List<Object> args) {
    this.commandName = commandName;
    this.command = command;
    this.name = name;
    this.options = options;
    this.args = args;
  }

  @Override
  CommandInvoker make() throws CommandCreationException {
    return command.resolveInvoker(
        name != null ? name : "",
        options != null ? options : Collections.<String, Object>emptyMap(),
        args != null ? args : Collections.<Object>emptyList());
  }

  private void format(Object o, StringBuilder buffer) {
    if (o instanceof String) {
      buffer.append('"').append(o).append('"');
    } else if (o instanceof Boolean || o instanceof Number) {
      buffer.append(o);
    } else {
      buffer.append('<').append(o).append('>');
    }
  }

  void toString(StringBuilder buffer) {
    buffer.append(commandName);
    boolean hasOptions = options != null && options.size() > 0;
    boolean hasArguments = args != null && args.size() > 0;
    if (hasOptions || hasArguments) {
      buffer.append(" {");
      if (hasOptions) {
        for (Iterator<Map.Entry<String, Object>> i = options.entrySet().iterator();i.hasNext();) {
          Map.Entry<String, Object> option = i.next();
          buffer.append(' ').append(option.getKey()).append('=');
          format(option.getValue(), buffer);
          if (i.hasNext()) {
            buffer.append(";");
          }
        }
        if (hasArguments) {
          buffer.append(";");
        }
      }
      if (hasArguments) {
        buffer.append(" [");
        for (Iterator<Object> i = args.iterator();i.hasNext();) {
          Object arg = i.next();
          format(arg, buffer);
          if (i.hasNext()) {
            buffer.append(", ");
          }
        }
        buffer.append("]");
      }
      buffer.append(" }");
    }
  }
}
