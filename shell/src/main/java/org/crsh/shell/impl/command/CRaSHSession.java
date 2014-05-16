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
package org.crsh.shell.impl.command;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.command.RuntimeContext;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.lang.script.ScriptRepl;
import org.crsh.plugin.PluginContext;
import org.crsh.repl.Repl;
import org.crsh.repl.ReplSession;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.repl.EvalResponse;
import org.crsh.shell.impl.command.spi.CommandManager;

import java.io.Closeable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRaSHSession extends HashMap<String, Object> implements Shell, Closeable, RuntimeContext, ReplSession {

  /** . */
  static final Logger log = Logger.getLogger(CRaSHSession.class.getName());

  /** . */
  static final Logger accessLog = Logger.getLogger("org.crsh.shell.access");

  /** . */
  public final CRaSH crash;

  /** . */
  final Principal user;

  /** . */
  private Repl repl = ScriptRepl.getInstance();

  CRaSHSession(final CRaSH crash, Principal user) {
    // Set variable available to all scripts
    put("crash", crash);

    //
    this.crash = crash;
    this.user = user;

    //


    //
    ClassLoader previous = setCRaSHLoader();
    try {
      for (CommandManager manager : crash.resolver.activeManagers.values()) {
        manager.init(this);
      }
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  /**
   * Returns the current repl of this session.
   *
   * @return the current repl
   */
  public Repl getRepl() {
    return repl;
  }

  /**
   * Set the current repl of this session.
   *
   * @param repl the new repl
   * @throws NullPointerException if the repl is null
   */
  public void setRepl(Repl repl) throws NullPointerException {
    if (repl == null) {
      throw new NullPointerException("No null repl accepted");
    }
    this.repl = repl;
  }

  public Iterable<String> getCommandNames() {
    return crash.getCommandNames();
  }

  public ShellCommand<?> getCommand(String name) throws CommandCreationException {
    return crash.getCommand(name);
  }

  public PluginContext getContext() {
    return crash.context;
  }

  public Map<String, Object> getSession() {
    return this;
  }

  public Map<String, Object> getAttributes() {
    return crash.context.getAttributes();
  }

  public void close() {
    ClassLoader previous = setCRaSHLoader();
    try {
      for (CommandManager manager : crash.resolver.activeManagers.values()) {
        manager.destroy(this);
      }
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    ClassLoader previous = setCRaSHLoader();
    try {
      CommandManager groovy = crash.resolver.activeManagers.get("groovy");
      if (groovy != null) {
        return groovy.doCallBack(this, "welcome", "");
      } else {
        return "";
      }
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public String getPrompt() {
    ClassLoader previous = setCRaSHLoader();
    try {
      CommandManager groovy = crash.resolver.activeManagers.get("groovy");
      if (groovy != null) {
        return groovy.doCallBack(this, "prompt", "% ");
      } else {
        return "% ";
      }
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public ShellProcess createProcess(String request) {
    log.log(Level.FINE, "Invoking request " + request);
    String trimmedRequest = request.trim();
    final StringBuilder msg = new StringBuilder();
    final ShellResponse response;
    if ("bye".equals(trimmedRequest) || "exit".equals(trimmedRequest)) {
      response = ShellResponse.close();
    } else {
      EvalResponse r = repl.eval(this, request);
      if (r instanceof EvalResponse.Response) {
        EvalResponse.Response rr = (EvalResponse.Response)r;
        response = rr.response;
      } else {
        final CommandInvoker<Void, ?> pipeLine = ((EvalResponse.Invoke)r).invoker;
        return new CRaSHCommandProcess(this, request, pipeLine);
      }
    }
    return new CRaSHResponseProcess(this, request, msg, response);
  }

  /**
   * For now basic implementation
   */
  public CompletionMatch complete(final String prefix) {
    ClassLoader previous = setCRaSHLoader();
    try {
      return repl.complete(this, prefix);
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  ClassLoader setCRaSHLoader() {
    Thread thread = Thread.currentThread();
    ClassLoader previous = thread.getContextClassLoader();
    thread.setContextClassLoader(crash.context.getLoader());
    return previous;
  }

  void setPreviousLoader(ClassLoader previous) {
    Thread.currentThread().setContextClassLoader(previous);
  }

}
