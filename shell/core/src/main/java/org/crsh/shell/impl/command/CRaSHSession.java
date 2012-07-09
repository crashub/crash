/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.impl.BaseCommandContext;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSHSession implements Shell, Closeable {

  /** . */
  static final Logger log = LoggerFactory.getLogger(CRaSHSession.class);

  /** . */
  static final Logger accessLog = LoggerFactory.getLogger("org.crsh.shell.access");

  /** . */
  private GroovyShell groovyShell;

  /** . */
  final CRaSH crash;

  /** . */
  final Map<String, Object> attributes;

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
      groovyShell = new GroovyShell(crash.context.getLoader(), new Binding(attributes), config);
    }
    return groovyShell;
  }

  public Script getLifeCycle(String name) throws NoSuchCommandException, NullPointerException {
    Class<? extends Script> scriptClass = crash.lifecycles.getClass(name);
    if (scriptClass != null) {
      Script script = InvokerHelper.createScript(scriptClass, new Binding(attributes));
      script.setBinding(new Binding(attributes));
      return script;
    } else {
      return null;
    }
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  CRaSHSession(final CRaSH crash, Principal user) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    // Set variable available to all scripts
    attributes.put("crash", crash);

    //
    this.attributes = attributes;
    this.groovyShell = null;
    this.crash = crash;
    this.user = user;

    //
    try {
      Script login = getLifeCycle("login");
      if (login != null) {
        login.run();
      }
    }
    catch (NoSuchCommandException e) {
      e.printStackTrace();
    }

  }

  public void close() {
    ClassLoader previous = setCRaSHLoader();
    try {
      Script login = getLifeCycle("logout");
      if (login != null) {
        login.run();
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

  public String getWelcome() {
    ClassLoader previous = setCRaSHLoader();
    try {
      GroovyShell shell = getGroovyShell();
      Object ret = shell.evaluate("welcome();");
      return String.valueOf(ret);
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public String getPrompt() {
    ClassLoader previous = setCRaSHLoader();
    try {
      GroovyShell shell = getGroovyShell();
      Object ret = shell.evaluate("prompt();");
      return String.valueOf(ret);
    }
    finally {
      setPreviousLoader(previous);
    }
  }

  public ShellProcess createProcess(String request) {
    log.debug("Invoking request " + request);
    final ShellResponse response;
    if ("bye".equals(request) || "exit".equals(request)) {
      response = ShellResponse.close();
    } else {

      // Create AST
      Parser parser = new Parser(request);
      AST ast = parser.parse();

      //
      if (ast instanceof AST.Expr) {
        AST.Expr expr = (AST.Expr)ast;

        // Create commands first
        try {
          return expr.create(this, request);
        } catch (NoSuchCommandException e) {
          log.error("Could not create shell process", e);
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
  public CommandCompletion complete(final String prefix) {
    ClassLoader previous = setCRaSHLoader();
    try {
      log.debug("Want prefix of " + prefix);
      AST ast = new Parser(prefix).parse();
      String termPrefix;
      if (ast != null) {
        AST.Term last = ast.lastTerm();
        termPrefix = Utils.trimLeft(last.getLine());
      } else {
        termPrefix = "";
      }

      //
      log.debug("Retained term prefix is " + prefix);
      CommandCompletion completion;
      int pos = termPrefix.indexOf(' ');
      if (pos == -1) {
        ValueCompletion completions = ValueCompletion.create();
        for (String resourceId : crash.context.listResourceId(ResourceKind.COMMAND)) {
          if (resourceId.startsWith(termPrefix)) {
            completions.put(resourceId.substring(termPrefix.length()), true);
          }
        }
        completion = new CommandCompletion(Delimiter.EMPTY, completions);
      } else {
        String commandName = termPrefix.substring(0, pos);
        termPrefix = termPrefix.substring(pos);
        try {
          ShellCommand command = crash.getCommand(commandName);
          if (command != null) {
            completion = command.complete(new BaseCommandContext(attributes, crash.context.getAttributes()), termPrefix);
          } else {
            completion = new CommandCompletion(Delimiter.EMPTY, ValueCompletion.create());
          }
        }
        catch (NoSuchCommandException e) {
          log.debug("Could not create command for completion of " + prefix, e);
          completion = new CommandCompletion(Delimiter.EMPTY, ValueCompletion.create());
        }
      }

      //
      log.debug("Found completions for " + prefix + ": " + completion);
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
