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
package org.crsh.lang.groovy;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.BaseShellCommand;
import org.crsh.command.CommandCreationException;
import org.crsh.command.ShellCommand;
import org.crsh.lang.groovy.command.GroovyScriptShellCommand;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.shell.impl.command.CommandResolution;
import org.crsh.util.AbstractClassCache;
import org.crsh.util.ClassCache;
import org.crsh.shell.impl.command.CommandManager;
import org.crsh.lang.groovy.command.GroovyScript;
import org.crsh.lang.groovy.command.GroovyScriptCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.ErrorType;
import org.crsh.util.TimestampedObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class GroovyCommandManagerImpl implements CommandManager {

  /** . */
  static final Logger log = Logger.getLogger(GroovyCommandManagerImpl.class.getName());

  /** . */
  private AbstractClassCache<GroovyScript> scriptCache;

  /** . */
  private GroovyClassFactory<Object> objectGroovyClassFactory;

  public GroovyCommandManagerImpl(PluginContext context) {
    this.objectGroovyClassFactory = new GroovyClassFactory<Object>(context.getLoader(), Object.class, GroovyScriptCommand.class);
    this.scriptCache = new ClassCache<GroovyScript>(context, new GroovyClassFactory<GroovyScript>(context.getLoader(), GroovyScript.class, GroovyScript.class), ResourceKind.LIFECYCLE);
  }

  public Set<String> getExtensions() {
    return GroovyCommandManager.EXT;
  }

  public boolean isActive() {
    return true;
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
    catch (CommandCreationException e) {
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
    catch (CommandCreationException e) {
      e.printStackTrace();
    }
  }

  /**
   * The underlying groovu shell used for the REPL.
   *
   * @return a groovy shell operating on the session attributes
   */
  public static GroovyShell getGroovyShell(CRaSHSession session) {
    GroovyShell shell = (GroovyShell)session.get("shell");
    if (shell == null) {
      CompilerConfiguration config = new CompilerConfiguration();
      config.setRecompileGroovySource(true);
      ShellBinding binding = new ShellBinding(session, session);
      shell = new GroovyShell(session.crash.getContext().getLoader(), binding, config);
      session.put("shell", shell);
    }
    return shell;
  }

  private String eval(HashMap<String, Object> session, String name, String def) {
    try {
      GroovyShell shell = getGroovyShell((CRaSHSession)session);
      Object ret = shell.getContext().getVariable(name);
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

  public GroovyScript getLifeCycle(HashMap<String, Object> session, String name) throws CommandCreationException, NullPointerException {
    TimestampedObject<Class<? extends GroovyScript>> ref = scriptCache.getClass(name);
    if (ref != null) {
      Class<? extends GroovyScript> scriptClass = ref.getObject();
      GroovyScript script = (GroovyScript)InvokerHelper.createScript(scriptClass, new Binding(session));
      script.setBinding(new Binding(session));
      return script;
    } else {
      return null;
    }
  }

  public CommandResolution resolveCommand(final String name, byte[] source) throws CommandCreationException, NullPointerException {

    //
    if (source == null) {
      throw new NullPointerException("No null command source allowed");
    }

    //
    final String script;
    try {
      script = new String(source, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not compile command script " + name, e);
    }

    // Get the description using a partial compilation because it is much faster than compiling the class
    // the class will be compiled lazyly
    String resolveDescription = null;
    CompilationUnit cu = new CompilationUnit(objectGroovyClassFactory.config);
    cu.addSource(name, script);
    try {
      cu.compile(Phases.CONVERSION);
    }
    catch (CompilationFailedException e) {
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not compile command", e);
    }
    CompileUnit ast = cu.getAST();
    if (ast.getClasses().size() > 0) {
      ClassNode classNode= (ClassNode)ast.getClasses().get(0);
      if (classNode != null) {
        for (AnnotationNode annotation : classNode.getAnnotations()) {
          if (annotation.getClassNode().getName().equals(Usage.class.getSimpleName())) {
            resolveDescription = annotation.getMember("value").getText();
            break;
          }
        }
        if (resolveDescription == null) {
          for (MethodNode main : classNode.getMethods("main")) {
            for (AnnotationNode annotation : main.getAnnotations()) {
              if (annotation.getClassNode().getName().equals(Usage.class.getSimpleName())) {
                resolveDescription = annotation.getMember("value").getText();
                break;
              }
            }
          }
        }
      }
    }
    final String description = resolveDescription;

    //
    return new CommandResolution() {
      ShellCommand command;
      @Override
      public String getDescription() {
        return description;
      }
      @Override
      public ShellCommand getCommand() throws CommandCreationException {
        if (command == null) {
          Class<?> clazz = objectGroovyClassFactory.parse(name, script);
          if (BaseCommand.class.isAssignableFrom(clazz)) {
            Class<? extends BaseCommand> cmd = clazz.asSubclass(BaseCommand.class);
            command = make(cmd);
          }
          else if (GroovyScriptCommand.class.isAssignableFrom(clazz)) {
            Class<? extends GroovyScriptCommand> cmd = clazz.asSubclass(GroovyScriptCommand.class);
            command = make2(cmd);
          }
          else {
            throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not create command " + name + " instance");
          }
        }
        return command;
      }
    };
  }

  private <C extends BaseCommand> BaseShellCommand<C> make(Class<C> clazz) {
    return new BaseShellCommand<C>(clazz);
  }
  private <C extends GroovyScriptCommand> GroovyScriptShellCommand<C> make2(Class<C> clazz) {
    return new GroovyScriptShellCommand<C>(clazz);
  }
}
