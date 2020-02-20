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
package org.crsh.lang.impl.groovy;

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
import org.crsh.cli.Usage;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.command.BaseCommand;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.lang.impl.groovy.command.GroovyScriptShellCommand;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;
import org.crsh.plugin.PluginContext;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Julien Viet */
public class GroovyCompiler implements org.crsh.lang.spi.Compiler {

  /** . */
  static final Logger log = Logger.getLogger(GroovyCompiler.class.getName());

  /** . */
  private static final Set<String> EXT = Collections.singleton("groovy");

  /** . */
  private GroovyClassFactory<Object> objectGroovyClassFactory;

  public GroovyCompiler(PluginContext context) {
    this.objectGroovyClassFactory = new GroovyClassFactory<Object>(context.getLoader(), Object.class, GroovyScriptCommand.class);
  }

  public Set<String> getExtensions() {
    return EXT;
  }

  public String doCallBack(ShellSession session, String name, String defaultValue) {
    return eval(session, name, defaultValue);
  }

  /**
   * The underlying groovu shell used for the REPL.
   *
   * @return a groovy shell operating on the session attributes
   */
  public static GroovyShell getGroovyShell(ShellSession session) {
    GroovyShell shell = (GroovyShell)session.get("shell");
    if (shell == null) {
      CompilerConfiguration config = new CompilerConfiguration();
      config.setRecompileGroovySource(true);
      ShellBinding binding = new ShellBinding(session, session);
      shell = new GroovyShell(session.getContext().getLoader(), binding, config);
      session.put("shell", shell);
    }
    return shell;
  }

  private String eval(ShellSession session, String name, String def) {
    try {
      GroovyShell shell = getGroovyShell(session);
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

  public CommandResolution compileCommand(final String name, byte[] source) throws CommandException, NullPointerException {

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
      throw new CommandException(ErrorKind.INTERNAL, "Could not compile command script " + name, e);
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
      throw new CommandException(ErrorKind.INTERNAL, "Could not compile command", e);
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
      Command<?> command;
      @Override
      public String getDescription() {
        return description;
      }
      @Override
      public Command<?> getCommand() throws CommandException {
        if (command == null) {
          Class<?> clazz = objectGroovyClassFactory.parse(name, script);
          if (BaseCommand.class.isAssignableFrom(clazz)) {
            Class<? extends BaseCommand> cmd = clazz.asSubclass(BaseCommand.class);
            try {
              command = make(cmd);
            }
            catch (IntrospectionException e) {
              throw new CommandException(ErrorKind.INTERNAL, "Invalid cli annotations for command " + name, e);
            }
          }
          else if (GroovyScriptCommand.class.isAssignableFrom(clazz)) {
            Class<? extends GroovyScriptCommand> cmd = clazz.asSubclass(GroovyScriptCommand.class);
            try {
              command = make2(cmd);
            }
            catch (IntrospectionException e) {
              throw new CommandException(ErrorKind.INTERNAL, "Invalid cli annotations for command " + name, e);
            }
          }
          else {
            throw new CommandException(ErrorKind.INTERNAL, "Could not create command " + name + " instance");
          }
        }
        return command;
      }
    };
  }

  private <C extends BaseCommand> ClassShellCommand<C> make(Class<C> clazz) throws IntrospectionException {
    return new ClassShellCommand<C>(clazz, ShellSafetyFactory.getCurrentThreadShellSafety());
  }

  private <C extends GroovyScriptCommand> GroovyScriptShellCommand<C> make2(Class<C> clazz) throws IntrospectionException {
    return new GroovyScriptShellCommand<C>(clazz);
  }
}
