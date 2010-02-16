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
package org.crsh.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.jcr.NodeMetaClass;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Shell {

  static {
    // Force integration of node meta class
    NodeMetaClass.setup();
  }

  /** . */
  private final GroovyShell groovyShell;

  /** . */
  private final StringBuffer out;

  /** . */
  private final ShellContext context;

  /** . */
  private final Map<String, Class<? extends ShellCommand>> commands;

  /** . */
  final CommandContext commandContext;

  /** . */
  private final ExecutorService executor;

  ShellCommand getClosure(String name) {
    Class<? extends ShellCommand> closure = commands.get(name);

    //
    if (closure == null) {
      String id = "/commands/" + name + ".groovy";
      String script = context.loadScript(id);
      if (script != null) {
        Class<?> clazz = groovyShell.getClassLoader().parseClass(script, id);
        if (ShellCommand.class.isAssignableFrom(clazz)) {
          closure = clazz.asSubclass(ShellCommand.class);
          commands.put(name, closure);
        } else {
          System.out.println("Parsed script does not implements " + ShellCommand.class.getName());
        }
      }
      else {
        return null;
      }
    }

    //
    try {
      return closure.newInstance();
    }
    catch (InstantiationException e) {
      throw new Error(e);
    }
    catch (IllegalAccessException e) {
      throw new Error(e);
    }
  }

  public GroovyShell getGroovyShell() {
    return groovyShell;
  }

  public Object getAttribute(String name) {
    return commandContext.get(name);
  }

  public void setAttribute(String name, Object value) {
    commandContext.put(name, value);
  }

  public Shell(ShellContext context) {
    this(context, null);
  }

  Shell(final ShellContext context, ExecutorService executor) {
    CommandContext commandContext = new CommandContext();

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(ScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(commandContext), config);


    // Evaluate login script
    String script = context.loadScript("/login.groovy");
    groovyShell.evaluate(script, "/login.groovy");

    //
    this.commandContext = commandContext;
    this.out = new StringBuffer();
    this.groovyShell = groovyShell;
    this.commands = new HashMap<String, Class<? extends ShellCommand>>();
    this.context = context;
    this.executor = executor;
  }

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadScript("/logout.groovy");
    groovyShell.evaluate(script, "/logout.groovy");
  }

  public ShellResponse evaluate(String s) {
    Evaluable evaluable = callable(s);
    return evaluable.evaluate();
  }

  public Future<ShellResponse> submitEvaluation(String s) {
    Evaluable callable = callable(s);
    if (executor != null) {
      return executor.submit(callable);
    } else {
      return new ImmediateFuture<ShellResponse>(callable.evaluate());
    }
  }

  private Evaluable callable(final String s) {
    return new Evaluable(this, s);
  }
}
