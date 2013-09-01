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

package org.crsh.lang.script;

import org.crsh.command.CommandCreationException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.repl.REPLSession;
import org.crsh.command.pipeline.PipeLine;
import org.crsh.text.Chunk;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * A factory for a pipeline.
 */
public class PipeLineFactory {

  /** . */
  final String line;

  /** . */
  final String name;

  /** . */
  final String rest;

  /** . */
  final PipeLineFactory next;

  public String getLine() {
    return line;
  }

  public PipeLineFactory getNext() {
    return next;
  }

  public PipeLineFactory(String line, PipeLineFactory next) {

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

  public CommandInvoker<Void, Chunk> create(REPLSession session) throws CommandCreationException {

    //
    LinkedList<CommandInvoker> pipes = new LinkedList<CommandInvoker>();
    for (PipeLineFactory current = this;current != null;current = current.next) {
      CommandInvoker commandInvoker = null;
      if (current.name != null) {
        ShellCommand command = session.getCommand(current.name);
        if (command != null) {
          commandInvoker = command.resolveInvoker(current.rest);
        }
      }
      if (commandInvoker == null) {
        throw new CommandCreationException(current.name);
      }
      pipes.add(commandInvoker);
    }

    //
    return new PipeLine(pipes.toArray(new CommandInvoker[pipes.size()]));
  }

  public PipeLineFactory getLast() {
    if (next != null) {
      return next.getLast();
    }
    return this;
  }
}
