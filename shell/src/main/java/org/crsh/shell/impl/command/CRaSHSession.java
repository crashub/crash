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

import org.crsh.auth.AuthInfo;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.lang.spi.Compiler;
import org.crsh.lang.spi.Language;
import org.crsh.lang.spi.Repl;
import org.crsh.lang.spi.ReplResponse;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.command.RuntimeContext;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.lang.impl.script.ScriptRepl;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;

import java.io.Closeable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRaSHSession extends HashMap<String, Object> implements Shell, Closeable, RuntimeContext, ShellSession {

  /** . */
  static final Logger log = Logger.getLogger(CRaSHSession.class.getName());

  /** . */
  static final Logger accessLog = Logger.getLogger("org.crsh.shell.access");

  /** . */
  public final CRaSH crash;

  /** . */
  final Principal user;

  final AuthInfo authInfo;

  final ShellSafety shellSafety;

  /** . */
  private Repl repl = ScriptRepl.getInstance();

  CRaSHSession(final CRaSH crash, Principal user, AuthInfo authInfo, ShellSafety shellSafety) {
    // Set variable available to all scripts
    put("crash", crash);

    //
    this.crash = crash;
    this.user = user;
    this.authInfo = authInfo;
    this.shellSafety = shellSafety;
    ShellSafetyFactory.registerShellSafetyForThread(this.shellSafety);

    //
    ClassLoader previous = setCRaSHLoader();
    try {
      for (Language manager : crash.langs) {
        manager.init(this);
      }
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public Repl getRepl() {
    return repl;
  }

  public void setRepl(Repl repl) throws NullPointerException {
    if (repl == null) {
      throw new NullPointerException("No null repl accepted");
    }
    this.repl = repl;
  }

  public AuthInfo getAuthInfo() {
    return authInfo;
  }

  public Iterable<Map.Entry<String, String>> getCommands() {
    return crash.getCommandsSafetyCheck(shellSafety);
  }

  public Command<?> getCommand(String name) throws CommandException {
    return crash.getCommandSafetyCheck(name, shellSafety);
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
      for (Language manager : crash.langs) {
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
      Compiler groovy = crash.scriptResolver.getCompiler("groovy");
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
      Compiler groovy = crash.scriptResolver.getCompiler("groovy");
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
    if (("bye".equals(trimmedRequest) || "exit".equals(trimmedRequest)) && shellSafety.permitExit()) {
      response = ShellResponse.close();
    } else {
      ReplResponse r = repl.eval(this, request);
      if (r instanceof ReplResponse.Response) {
        ReplResponse.Response rr = (ReplResponse.Response)r;
        response = rr.response;
      } else {
        final CommandInvoker<Void, ?> pipeLine = ((ReplResponse.Invoke)r).invoker;
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
