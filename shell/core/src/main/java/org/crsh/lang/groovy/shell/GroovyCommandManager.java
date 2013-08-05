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
package org.crsh.lang.groovy.shell;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.ShellCommand;
import org.crsh.lang.CommandManager;
import org.crsh.lang.groovy.ShellBinding;
import org.crsh.lang.groovy.command.GroovyScript;
import org.crsh.lang.groovy.command.GroovyScriptCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class GroovyCommandManager extends CommandManager {

  /** . */
  static final Logger log = Logger.getLogger(GroovyCommandManager.class.getName());

  /** . */
  final PluginContext context;

  /** . */
  final AbstractClassManager<? extends ShellCommand> commandManager;

  /** . */
  final AbstractClassManager<? extends GroovyScript> scriptManager;

  public GroovyCommandManager(PluginContext context) {
    this.context = context;
    this.commandManager = new ClassManager<ShellCommand>(context, ResourceKind.COMMAND, ShellCommand.class, GroovyScriptCommand.class);
    this.scriptManager = new ClassManager<GroovyScript>(context, ResourceKind.LIFECYCLE, GroovyScript.class, GroovyScript.class);
  }

  public String doCallBack(HashMap<String, Object> session, String name, String defaultValue) {
    return eval(session, name, defaultValue);
  }

  public void init(HashMap<String, Object> session) {
    try {
      GroovyScript login = getLifeCycle(session, "login");
      if (login != null) {
        login.setBinding(new Binding(session));
        login.run();
      }
    }
    catch (NoSuchCommandException e) {
      e.printStackTrace();
    }
  }

  public void destroy(HashMap<String, Object> session) {
    try {
      GroovyScript logout = getLifeCycle(session, "logout");
      if (logout != null) {
        logout.setBinding(new Binding(session));
        logout.run();
      }
    }
    catch (NoSuchCommandException e) {
      e.printStackTrace();
    }
  }

  public GroovyShell getGroovyShell(Map<String, Object> session) {
    return getGroovyShell(context, session);
  }

  /**
   * The underlying groovu shell used for the REPL.
   *
   * @return a groovy shell operating on the session attributes
   */
  public static GroovyShell getGroovyShell(PluginContext context, Map<String, Object> session) {
    GroovyShell shell = (GroovyShell)session.get("shell");
    if (shell == null) {
      CompilerConfiguration config = new CompilerConfiguration();
      config.setRecompileGroovySource(true);
      ShellBinding binding = new ShellBinding(session);
      shell = new GroovyShell(context.getLoader(), binding, config);
      session.put("shell", shell);
    }
    return shell;
  }

  private String eval(HashMap<String, Object> session, String name, String def) {
    try {
      GroovyShell shell = getGroovyShell(session);
      Object ret = shell.evaluate("return " + name + ";");
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
  }

  public GroovyScript getLifeCycle(HashMap<String, Object> session, String name) throws NoSuchCommandException, NullPointerException {
    Class<? extends GroovyScript> scriptClass = scriptManager.getClass(name);
    if (scriptClass != null) {
      GroovyScript script = (GroovyScript)InvokerHelper.createScript(scriptClass, new Binding(session));
      script.setBinding(new Binding(session));
      return script;
    } else {
      return null;
    }
  }

  public ShellCommand getCommand(String name) throws NoSuchCommandException, NullPointerException {
    return commandManager.getInstance(name);
  }
}
