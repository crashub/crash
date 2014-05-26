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
package org.crsh.lang.impl.groovy.closure;

import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** @author Julien Viet */
class CommandElement extends PipeLineElement {

  /** . */
  final String commandName;

  /** . */
  final Command<?> command;

  /** . */
  final Map<String, Object> options;

  /** . */
  final String subordinate;

  /** . */
  final Map<String, Object> subordinateOptions;

  /** . */
  final List<Object> args;

  public CommandElement(String commandName, Command<?> command, Map<String, Object> options) {
    this.commandName = commandName;
    this.command = command;
    this.options = options;
    this.subordinate = null;
    this.subordinateOptions = null;
    this.args = null;
  }

  public CommandElement subordinate(String name) {
    return new CommandElement(
        this.commandName + "." + name,
        this.command,
        this.options,
        name,
        this.subordinateOptions,
        this.args);
  }

  public CommandElement merge(Map<String, ?> options, List<?> arguments) {

    // We merge options
    Map<String, Object> nextOptions;
    if (subordinate == null) {
      nextOptions = this.options;
    } else {
      nextOptions = this.subordinateOptions;
    }
    if (options != null && options.size() > 0) {
      if (nextOptions == null) {
        nextOptions = new HashMap<String, Object>();
      } else {
        nextOptions = new HashMap<String, Object>(options);
      }
      for (Map.Entry<?, ?> arg : options.entrySet()) {
        nextOptions.put(arg.getKey().toString(), arg.getValue());
      }
    }

    // We merge arguments
    List<Object> nextArgs;
    if (arguments != null) {
      nextArgs = new ArrayList<Object>();
      if (this.args != null) {
        nextArgs.addAll(this.args);
      }
      nextArgs.addAll(arguments);
    } else {
      nextArgs = this.args;
    }

    //
    if (subordinate == null) {
      return new CommandElement(this.commandName, this.command, nextOptions, null, null, nextArgs);
    } else {
      return new CommandElement(this.commandName, this.command, this.options, subordinate, nextOptions, nextArgs);
    }
  }

  private CommandElement(String commandName, Command<?> command, Map<String, Object> options, String subordinate, Map<String, Object> subordinateOptions, List<Object> args) {
    this.commandName = commandName;
    this.command = command;
    this.options = options;
    this.subordinate = subordinate;
    this.subordinateOptions = subordinateOptions;
    this.args = args;
  }

  @Override
  CommandInvoker create() throws CommandException {
    return command.resolveCommand(options, subordinate, subordinateOptions, args).getInvoker();
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
    boolean hasOptions = subordinateOptions != null && subordinateOptions.size() > 0;
    boolean hasArguments = args != null && args.size() > 0;
    if (hasOptions || hasArguments) {
      buffer.append(" {");
      if (hasOptions) {
        for (Iterator<Map.Entry<String, Object>> i = subordinateOptions.entrySet().iterator();i.hasNext();) {
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
