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
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ErrorType;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.util.TimestampedObject;
import org.crsh.util.Utils;
import org.crsh.vfs.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSH implements Shell {

  /** . */
  static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final GroovyShell groovyShell;

  /** . */
  private final PluginContext context;

  /** . */
  private final Map<String, TimestampedObject<Class<? extends ShellCommand>>> commands;

  /** . */
  final Map<String, Object> attributes;

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws CreateCommandException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand getCommand(String name) throws CreateCommandException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null argument alloed");
    }

    TimestampedObject<Class<? extends ShellCommand>> providerRef = commands.get(name);

    //
    Resource script = context.loadResource(name, ResourceKind.SCRIPT);

    //
    if (script != null) {
      if (providerRef != null) {
        if (script.getTimestamp() != providerRef.getTimestamp()) {
          providerRef = null;
        }
      }

      //
      if (providerRef == null) {

        Class<?> clazz;
        try {
          clazz = groovyShell.getClassLoader().parseClass(script.getContent(), name);
        }
        catch (CompilationFailedException e) {
          throw new CreateCommandException(ErrorType.INTERNAL, "Could not compile command script", e);
        }

        //
        if (ShellCommand.class.isAssignableFrom(clazz)) {
          Class<? extends ShellCommand> providerClass = clazz.asSubclass(ShellCommand.class);
          providerRef = new TimestampedObject<Class<? extends ShellCommand>>(script.getTimestamp(), providerClass);
          commands.put(name, providerRef);
        } else {
          throw new CreateCommandException(ErrorType.INTERNAL, "Parsed script " + clazz.getName() +
            " does not implements " + CommandInvoker.class.getName());
        }
      }
    }

    //
    if (providerRef == null) {
      return null;
    }

    //
    try {
      return providerRef.getObject().newInstance();
    }
    catch (Exception e) {
      throw new CreateCommandException(ErrorType.INTERNAL, "Could not create command " + providerRef.getObject().getName() + " instance", e);
    }
  }

  public GroovyShell getGroovyShell() {
    return groovyShell;
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  public CRaSH(final PluginContext context) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    // Set variable available to all scripts
    attributes.put("shellContext", context);
    attributes.put("shell", this);

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(attributes), config);

    // Evaluate login script
    String script = context.loadResource("login", ResourceKind.LIFECYCLE).getContent();
    groovyShell.evaluate(script, "login");

    //
    this.attributes = attributes;
    this.groovyShell = groovyShell;
    this.commands = new ConcurrentHashMap<String, TimestampedObject<Class<? extends ShellCommand>>>();
    this.context = context;
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadResource("logout", ResourceKind.LIFECYCLE).getContent();
    groovyShell.evaluate(script, "logout");
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return groovyShell.evaluate("welcome();").toString();
  }

  public String getPrompt() {
    return groovyShell.evaluate("prompt();").toString();
  }

  public ShellProcess createProcess(String request) {
    log.debug("Invoking request " + request);

    //
    CRaSHProcessFactory factory = new CRaSHProcessFactory(this, request);

    //
    return factory.create();
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
      for (String resourceId : context.listResourceId(ResourceKind.SCRIPT)) {
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
          completions = command.complete(new CommandContextImpl(attributes), termPrefix);
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
