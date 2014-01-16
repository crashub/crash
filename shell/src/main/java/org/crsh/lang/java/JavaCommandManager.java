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
package org.crsh.lang.java;

import org.crsh.command.BaseShellCommand;
import org.crsh.command.CommandCreationException;
import org.crsh.command.DescriptionFormat;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.ErrorType;
import org.crsh.shell.impl.command.CommandManager;
import org.crsh.shell.impl.command.CommandResolution;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/** @author Julien Viet */
public class JavaCommandManager extends CRaSHPlugin<CommandManager> implements CommandManager {

  /** . */
  private static final Set<String> EXT = Collections.singleton("java");

  /** . */
  private Compiler compiler;

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    ClassLoader loader = context.getLoader();
    Compiler compiler = new Compiler(loader);

    //
    this.compiler = compiler;
  }

  @Override
  public CommandManager getImplementation() {
    return this;
  }

  public Set<String> getExtensions() {
    return EXT;
  }

  public CommandResolution resolveCommand(String name, byte[] source) throws CommandCreationException, NullPointerException {
    String script = new String(source);
    List<JavaClassFileObject> classFiles;
    try {
      classFiles = compiler.compile(name, script);
    }
    catch (IOException e) {
      throw new CommandCreationException(name, ErrorType.INTERNAL, "Could not access command", e);
    }
    catch (CompilationFailureException e) {
        throw new CommandCreationException(name, ErrorType.EVALUATION, "Could not compile command", e);
    }
    for (JavaClassFileObject classFile : classFiles) {
      String className = classFile.getClassName();
      String simpleName = className.substring(className.lastIndexOf('.') + 1);
      if (simpleName.equals(name)) {
        LoadingClassLoader loader = new LoadingClassLoader(getContext().getLoader(), classFiles);
        try {
          Class<?> clazz = loader.loadClass(classFile.getClassName());
          final BaseShellCommand command = new BaseShellCommand(clazz);
          final String description = command.describe(name, DescriptionFormat.DESCRIBE);
          return new CommandResolution() {
            @Override
            public String getDescription() {
              return description;
            }
            @Override
            public ShellCommand getCommand() throws CommandCreationException {
              return command;
            }
          };
        }
        catch (ClassNotFoundException e) {
          throw new CommandCreationException(name, ErrorType.EVALUATION, "Command cannot be loaded", e);
        }
      }
    }
    throw new CommandCreationException(name, ErrorType.EVALUATION, "Command class not found");
  }

  public void init(HashMap<String, Object> session) {
    //
  }

  public void destroy(HashMap<String, Object> session) {
    //
  }

  public String doCallBack(HashMap<String, Object> session, String name, String defaultValue) {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
