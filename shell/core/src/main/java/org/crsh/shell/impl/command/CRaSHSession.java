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
import org.crsh.command.RuntimeContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.lang.CommandManager;
import org.crsh.plugin.PluginContext;
import org.crsh.repl.REPL;
import org.crsh.shell.ErrorType;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.repl.EvalResponse;
import org.crsh.lang.script.ScriptREPL;
import org.crsh.repl.REPLSession;
import org.crsh.text.Text;
import org.crsh.util.Safe;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRaSHSession extends HashMap<String, Object> implements Shell, Closeable, RuntimeContext, REPLSession {

  /** . */
  static final Logger log = Logger.getLogger(CRaSHSession.class.getName());

  /** . */
  static final Logger accessLog = Logger.getLogger("org.crsh.shell.access");

  /** . */
  public final CRaSH crash;

  /** . */
  final Principal user;

  public CommandManager getCommandManager() {
    return crash.commandManager;
  }

  CRaSHSession(final CRaSH crash, Principal user) {
    // Set variable available to all scripts
    put("crash", crash);

    //
    this.crash = crash;
    this.user = user;

    //
    ClassLoader previous = setCRaSHLoader();
    try {
      crash.commandManager.init(this);
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public ShellCommand getCommand(String name) throws NoSuchCommandException {
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
      crash.commandManager.destroy(this);
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    ClassLoader previous = setCRaSHLoader();
    try {
      return crash.commandManager.doCallBack(this, "welcome", "");
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public String getPrompt() {
    ClassLoader previous = setCRaSHLoader();
    try {
      return crash.commandManager.doCallBack(this, "prompt", "% ");
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  /** . */
  private REPL repl = new ScriptREPL();

  public ShellProcess createProcess(String request) {
    log.log(Level.FINE, "Invoking request " + request);
    String trimmedRequest = request.trim();
    final StringBuilder msg = new StringBuilder();
    final ShellResponse response;
    if ("bye".equals(trimmedRequest) || "exit".equals(trimmedRequest)) {
      response = ShellResponse.close();
    } else if (trimmedRequest.equals("repl")) {
      msg.append("Current repl ").append(repl.getName());
      response = ShellResponse.ok();
    } else if (trimmedRequest.startsWith("repl ")) {
      String name = trimmedRequest.substring("repl ".length()).trim();
      if (name.equals(repl.getName())) {
        response = ShellResponse.ok();
      } else {
        REPL found = null;
        for (REPL repl : ServiceLoader.load(REPL.class)) {
          if (repl.getName().equals(name)) {
            found = repl;
            break;
          }
        }
        if (found != null) {
          repl = found;
          msg.append("Using repl ").append(found.getName());
          response = ShellResponse.ok();
        } else {
          response = ShellResponse.error(ErrorType.EVALUATION, "Repl " + name + " not found");
        }
      }
    } else {
      EvalResponse r = repl.eval(this, request);
      if (r instanceof EvalResponse.Response) {
        EvalResponse.Response rr = (EvalResponse.Response)r;
        response = rr.response;
      } else {
        final CommandInvoker<Void, ?> pipeLine = ((EvalResponse.Invoke)r).invoker;
        return new CRaSHProcess(this, request) {

          @Override
          ShellResponse doInvoke(final ShellProcessContext context) throws InterruptedException {
            CRaSHProcessContext invocationContext = new CRaSHProcessContext(CRaSHSession.this, context);
            try {
              pipeLine.invoke(invocationContext);
              return ShellResponse.ok();
            }
            catch (ScriptException e) {
              return build(e);
            } catch (Throwable t) {
              return build(t);
            } finally {
              Safe.close(invocationContext);
            }
          }

          private ShellResponse.Error build(Throwable throwable) {
            ErrorType errorType;
            if (throwable instanceof ScriptException || throwable instanceof UndeclaredThrowableException) {
              errorType = ErrorType.EVALUATION;
              Throwable cause = throwable.getCause();
              if (cause != null) {
                throwable = cause;
              }
            } else {
              errorType = ErrorType.INTERNAL;
            }
            String result;
            String msg = throwable.getMessage();
            if (throwable instanceof ScriptException) {
              if (msg == null) {
                result = request + ": failed";
              } else {
                result = request + ": " + msg;
              }
              return ShellResponse.error(errorType, result, throwable);
            } else {
              if (msg == null) {
                msg = throwable.getClass().getSimpleName();
              }
              if (throwable instanceof RuntimeException) {
                result = request + ": exception: " + msg;
              } else if (throwable instanceof Exception) {
                result = request + ": exception: " + msg;
              } else if (throwable instanceof java.lang.Error) {
                result = request + ": error: " + msg;
              } else {
                result = request + ": unexpected throwable: " + msg;
              }
              return ShellResponse.error(errorType, result, throwable);
            }
          }
        };
      }
    }
    return new CRaSHProcess(this, request) {
      @Override
      ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {
        if (msg.length() > 0) {
          try {
            context.write(Text.create(msg));
          }
          catch (IOException ignore) {
          }
        }
        return response;
      }
    };
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
