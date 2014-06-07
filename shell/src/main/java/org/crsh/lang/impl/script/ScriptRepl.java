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

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.lang.spi.*;
import org.crsh.shell.impl.command.RuntimeContextImpl;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.ShellResponse;
import org.crsh.util.Utils;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class ScriptRepl implements Repl {

  /** . */
  private static final ScriptRepl instance = new ScriptRepl();

  /** . */
  static final Logger log = Logger.getLogger(ScriptRepl.class.getName());

  public static ScriptRepl getInstance() {
    return instance;
  }

  /** . */
  private final Language lang = new Language() {
    @Override public String getName() { return "script"; }
    @Override public String getDisplayName() { return "Script 1.0"; }
    @Override public boolean isActive() { return true; }
    @Override public Repl getRepl() { return ScriptRepl.this; }
    @Override public org.crsh.lang.spi.Compiler getCompiler() { return ScriptCompiler.instance; }
    @Override public void init(ShellSession session) { }
    @Override public void destroy(ShellSession session) { }
  };

  private ScriptRepl() {
  }

  @Override
  public Language getLanguage() {
    return lang;
  }

  public String getName() {
    return "script";
  }

  @Override
  public String getDescription() {
    return "The Script repl provides command line interpreter with a bash like syntax";
  }

  public ReplResponse eval(ShellSession session, String request) {
    PipeLineFactory factory;
    try {
      factory = Token.parse(request).createFactory();
    }
    catch (CommandException e) {
      return new ReplResponse.Response(ShellResponse.error(e.getErrorKind(), e.getMessage(), e.getCause()));
    }
    if (factory != null) {
      try {
        CommandInvoker<Void, Object> invoker = factory.create(session);
        return new ReplResponse.Invoke(invoker);
      }
      catch (CommandNotFoundException e) {
        log.log(Level.FINER, "Could not create command", e);
        return new ReplResponse.Response(ShellResponse.unknownCommand(e.getName()));
      }
      catch (CommandException e) {
        log.log(Level.FINER, "Could not create command", e);
        return new ReplResponse.Response(ShellResponse.error(e.getErrorKind(), e.getMessage(), e));
      }
    } else {
      return new ReplResponse.Response(ShellResponse.noCommand());
    }
  }

  public CompletionMatch complete(ShellSession session, String prefix) {
    Token ast = Token.parse(prefix);
    String termPrefix;
    if (ast != null) {
      Token last = ast.getLast();
      termPrefix = Utils.trimLeft(last.value);
    } else {
      termPrefix = "";
    }

    //
    log.log(Level.FINE, "Retained term prefix is " + termPrefix);
    CompletionMatch completion;
    int pos = termPrefix.indexOf(' ');
    if (pos == -1) {
      Completion.Builder builder = Completion.builder(termPrefix);
      for (Map.Entry<String, String> command : session.getCommands()) {
        String name = command.getKey();
        if (name.startsWith(termPrefix)) {
          builder.add(name.substring(termPrefix.length()), true);
        }
      }
      completion = new CompletionMatch(Delimiter.EMPTY, builder.build());
    } else {
      String commandName = termPrefix.substring(0, pos);
      termPrefix = termPrefix.substring(pos);
      try {
        Command<?> command = session.getCommand(commandName);
        if (command != null) {
          completion = command.complete(new RuntimeContextImpl(session, session.getContext().getAttributes()), termPrefix);
        } else {
          completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
        }
      }
      catch (CommandException e) {
        log.log(Level.FINE, "Could not create command for completion of " + prefix, e);
        completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
      }
    }

    //
    return completion;
  }
}
