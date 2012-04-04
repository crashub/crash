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
package org.crsh.shell.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
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
import java.util.Collections;
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
  private GroovyShell groovyShell;

  /** . */
  private final CRaSH crash;

  /** . */
  final Map<String, Object> attributes;

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

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws CreateCommandException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand getCommand(String name) throws CreateCommandException, NullPointerException {
    return crash.commands.getInstance(name);
  }

  public Script getLifeCycle(String name) throws CreateCommandException, NullPointerException {
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

  CRaSHSession(final CRaSH crash) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    // Set variable available to all scripts
    attributes.put("shellContext", crash.context);
    attributes.put("shell", this);

    //
    this.attributes = attributes;
    this.groovyShell = null;
    this.crash = crash;

    //
    try {
      Script login = getLifeCycle("login");
      if (login != null) {
        login.run();
      }
    }
    catch (CreateCommandException e) {
      e.printStackTrace();
    }

  }

  public void close() {
    try {
      Script login = getLifeCycle("logout");
      if (login != null) {
        login.run();
      }
    }
    catch (CreateCommandException e) {
      e.printStackTrace();
    }
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    GroovyShell shell = getGroovyShell();
    Object ret = shell.evaluate("welcome();");
    return String.valueOf(ret);
  }

  public String getPrompt() {
    GroovyShell shell = getGroovyShell();
    Object ret = shell.evaluate("prompt();");
    return String.valueOf(ret);
  }

  public ShellProcess createProcess(String request) {
    log.debug("Invoking request " + request);
    final ShellResponse response;
    if ("bye".equals(request) || "exit".equals(request)) {
      response = new ShellResponse.Close();
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
        } catch (CreateCommandException e) {
          response = e.getResponse();
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
  public Map<String, String> complete(final String prefix) {
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
    Map<String, String> completions = Collections.emptyMap();
    int pos = termPrefix.indexOf(' ');
    if (pos == -1) {
      completions = new HashMap<String, String>();
      for (String resourceId : crash.context.listResourceId(ResourceKind.COMMAND)) {
        if (resourceId.startsWith(termPrefix)) {
          completions.put(resourceId.substring(termPrefix.length()), " ");
        }
      }
    } else {
      String commandName = termPrefix.substring(0, pos);
      termPrefix = termPrefix.substring(pos);
      try {
        ShellCommand command = getCommand(commandName);
        if (command != null) {
          completions = command.complete(new BaseCommandContext(attributes), termPrefix);
        }
      }
      catch (CreateCommandException e) {
        log.debug("Could not create command for completion of " + prefix, e);
      }
    }

    //
    log.debug("Found completions for " + prefix + ": " + completions);
    return completions;
  }
}
