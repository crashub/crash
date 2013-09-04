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

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseRuntimeContext;
import org.crsh.command.CommandCreationException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ShellResponse;
import org.crsh.repl.EvalResponse;
import org.crsh.repl.REPL;
import org.crsh.repl.REPLSession;
import org.crsh.text.Chunk;
import org.crsh.util.Utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class ScriptREPL implements REPL {

  /** . */
  static final Logger log = Logger.getLogger(ScriptREPL.class.getName());

  public String getName() {
    return "script";
  }

  public EvalResponse eval(REPLSession session, String request) {
    PipeLineParser parser = new PipeLineParser(request);
    final PipeLineFactory factory = parser.parse();
    if (factory != null) {
      try {
        CommandInvoker<Void, Chunk> invoker = factory.create(session);
        return new EvalResponse.Invoke(invoker);
      }
      catch (CommandCreationException e) {
        //e.printStackTrace();
        return new EvalResponse.Response(ShellResponse.unknownCommand(e.getCommandName()));
      }
    } else {
      return new EvalResponse.Response(ShellResponse.noCommand());
    }
  }

  public CompletionMatch complete(REPLSession session, String prefix) {
    PipeLineFactory ast = new PipeLineParser(prefix).parse();
    String termPrefix;
    if (ast != null) {
      PipeLineFactory last = ast.getLast();
      termPrefix = Utils.trimLeft(last.getLine());
    } else {
      termPrefix = "";
    }

    //
    log.log(Level.FINE, "Retained term prefix is " + prefix);
    CompletionMatch completion;
    int pos = termPrefix.indexOf(' ');
    if (pos == -1) {
      Completion.Builder builder = Completion.builder(prefix);
      for (String name : session.getCommandNames()) {
        if (name.startsWith(termPrefix)) {
          builder.add(name.substring(termPrefix.length()), true);
        }
      }
      completion = new CompletionMatch(Delimiter.EMPTY, builder.build());
    } else {
      String commandName = termPrefix.substring(0, pos);
      termPrefix = termPrefix.substring(pos);
      try {
        ShellCommand command = session.getCommand(commandName);
        if (command != null) {
          completion = command.complete(new BaseRuntimeContext(session, session.getContext().getAttributes()), termPrefix);
        } else {
          completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
        }
      }
      catch (CommandCreationException e) {
        log.log(Level.FINE, "Could not create command for completion of " + prefix, e);
        completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
      }
    }

    //
    return completion;
  }
}
