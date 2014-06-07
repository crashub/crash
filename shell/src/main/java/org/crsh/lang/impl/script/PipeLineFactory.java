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

package org.crsh.lang.impl.script;

import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.pipeline.PipeLine;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * A factory for a pipeline.
 */
public class PipeLineFactory {

  /** . */
  private static final Pattern p = Pattern.compile("^\\s*(\\S+)");

  /** . */
  final String line;

  /** . */
  final String name;

  /** . */
  final String rest;

  /** . */
  final PipeLineFactory next;

  /**
   * Create a pipeline factory for the specified line and next factory
   *
   * @param line the line
   * @param next the next factory
   * @throws CommandException when the line is not correct
   */
  public PipeLineFactory(String line, PipeLineFactory next) throws CommandException {
    java.util.regex.Matcher m = p.matcher(line);
    if (m.find()) {
      this.name = m.group(1);
      this.rest = line.substring(m.end());
      this.line = line;
      this.next = next;
    } else {
      StringBuilder sb = new StringBuilder("syntax error near unexpected token");
      if (next != null) {
        sb.append(' ');
        for (PipeLineFactory factory = next;factory != null;factory = factory.next) {
          sb.append('|').append(factory.line);
        }
      }
      throw new CommandException(ErrorKind.SYNTAX, sb.toString());
    }
  }

  public String getLine() {
    return line;
  }

  public PipeLineFactory getNext() {
    return next;
  }

  public CommandInvoker<Void, Object> create(ShellSession session) throws CommandNotFoundException, CommandException {
    LinkedList<CommandInvoker> pipes = new LinkedList<CommandInvoker>();
    for (PipeLineFactory current = this;current != null;current = current.next) {
      Command<?> command = session.getCommand(current.name);
      if (command == null) {
        throw new CommandNotFoundException(current.name);
      }
      CommandInvoker commandInvoker = command.resolveInvoker(current.rest);
      if (commandInvoker == null) {
        throw new CommandNotFoundException(current.name);
      }
      pipes.add(commandInvoker);
    }
    return new PipeLine(pipes.toArray(new CommandInvoker[pipes.size()]));
  }

  public PipeLineFactory getLast() {
    if (next != null) {
      return next.getLast();
    }
    return this;
  }
}
