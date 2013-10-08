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

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.BaseRuntimeContext;
import org.crsh.command.RuntimeContext;
import org.crsh.cli.impl.Delimiter;
import org.crsh.command.CommandInvoker;
import org.crsh.command.GroovyScript;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ErrorType;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Chunk;
import org.crsh.util.Safe;
import org.crsh.util.Utils;

import java.io.Closeable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRaSHSession extends HashMap<String, Object> implements Shell, Closeable, RuntimeContext {

  /** . */
  static final Logger log = Logger.getLogger(CRaSHSession.class.getName());

  /** . */
  static final Logger accessLog = Logger.getLogger("org.crsh.shell.access");

  /** . */
  private GroovyShell groovyShell;

  /** . */
  final CRaSH crash;

  /** . */
  final Principal user;

  /**
   * Used for testing purposes.
   *
   * @return a groovy shell operating on the session attributes
   */
  public GroovyShell getGroovyShell() {
    if (groovyShell == null) {
      CompilerConfiguration config = new CompilerConfiguration();
      config.setRecompileGroovySource(true);
      config.setScriptBaseClass(GroovyScriptCommand.class.getName());
      groovyShell = new GroovyShell(crash.context.getLoader(), new Binding(this), config);
    }
    return groovyShell;
  }

  public GroovyScript getLifeCycle(String name) throws NoSuchCommandException, NullPointerException {
    Class<? extends GroovyScript> scriptClass = crash.scriptManager.getClass(name);
    if (scriptClass != null) {
      GroovyScript script = (GroovyScript)InvokerHelper.createScript(scriptClass, new Binding(this));
      script.setBinding(new Binding(this));
      return script;
    } else {
      return null;
    }
  }

  CRaSHSession(final CRaSH crash, Principal user) {
    // Set variable available to all scripts
    put("crash", crash);

    //
    this.groovyShell = null;
    this.crash = crash;
    this.user = user;

    //
    try {
      GroovyScript login = getLifeCycle("login");
      if (login != null) {
        login.setContext(this);
        login.run();
      }
    }
    catch (NoSuchCommandException e) {
      e.printStackTrace();
    }

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
      GroovyScript logout = getLifeCycle("logout");
      if (logout != null) {
        logout.setContext(this);
        logout.run();
      }
    }
    catch (NoSuchCommandException e) {
      e.printStackTrace();
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  // Shell implementation **********************************************************************************************
  private HashMap<String, Script> parsedScripts = new HashMap<String, Script>();

  private String eval(String name, String def) {
    ClassLoader previous = setCRaSHLoader();
    try {
      GroovyShell shell = getGroovyShell();
      Script script = parsedScripts.get(name);
      if (script == null) {
        script = shell.parse("return " + name + ";");
        parsedScripts.put(name, script);
      }
      Object ret = script.run();
      if (ret instanceof Closure) {
        log.log(Level.FINEST, "Invoking " + name + " closure");
        Closure c = (Closure)ret;
        ret = c.call();
      } else if (ret == null) {
        log.log(Level.FINEST, "No " + name + " will use empty");
        return def;
      }
      return String.valueOf(ret);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Could not get a " + name + " message, will use empty", e);
      return def;
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public String getWelcome() {
    return eval("welcome", "");
  }

  public String getPrompt() {
    return eval("prompt", "% ");
  }

  public ShellProcess createProcess(String request) {
    log.log(Level.FINE, "Invoking request " + request);
    final ShellResponse response;
    if ("bye".equals(request) || "exit".equals(request)) {
      response = ShellResponse.close();
    } else {
      // Create pipeline from request
      PipeLineParser parser = new PipeLineParser(request);
      final PipeLineFactory factory = parser.parse();
      if (factory != null) {
        try {
          final CommandInvoker<Void, Chunk> pipeLine = factory.create(this);
          return new CRaSHProcess(this, request) {

            @Override
            ShellResponse doInvoke(final ShellProcessContext context) throws InterruptedException {
              CRaSHProcessContext invocationContext = new CRaSHProcessContext(CRaSHSession.this, context);
              try {
                pipeLine.open(invocationContext);
                pipeLine.flush();
                return ShellResponse.ok();
              }
              catch (ScriptException e) {
                return build(e);
              } catch (Throwable t) {
                return build(t);
              } finally {
                Safe.close(pipeLine);
                Safe.close(invocationContext);
              }
            }

            private ShellResponse.Error build(Throwable throwable) {
              ErrorType errorType;
              if (throwable instanceof ScriptException) {
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
        catch (NoSuchCommandException e) {
          response = ShellResponse.unknownCommand(e.getCommandName());
        }
      } else {
        response = ShellResponse.noCommand();
      }
    }

    //
    return new CRaSHProcess(this, request) {
      @Override
      ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {
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
      log.log(Level.FINE, "Want prefix of " + prefix);
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
        for (String resourceId : crash.context.listResourceId(ResourceKind.COMMAND)) {
          if (resourceId.startsWith(termPrefix)) {
            builder.add(resourceId.substring(termPrefix.length()), true);
          }
        }
        completion = new CompletionMatch(Delimiter.EMPTY, builder.build());
      } else {
        String commandName = termPrefix.substring(0, pos);
        termPrefix = termPrefix.substring(pos);
        try {
          ShellCommand command = crash.getCommand(commandName);
          if (command != null) {
            completion = command.complete(new BaseRuntimeContext(this, crash.context.getAttributes()), termPrefix);
          } else {
            completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
          }
        }
        catch (NoSuchCommandException e) {
          log.log(Level.FINE, "Could not create command for completion of " + prefix, e);
          completion = new CompletionMatch(Delimiter.EMPTY, Completion.create());
        }
      }

      //
      log.log(Level.FINE, "Found completions for " + prefix + ": " + completion);
      return completion;
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
