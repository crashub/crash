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
import org.crsh.command.ShellCommand;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.CommandManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/** @author Julien Viet */
public class JavaCommandManager extends CRaSHPlugin<CommandManager> implements CommandManager {

  /** . */
  private static final Set<String> EXT = Collections.singleton("java");

  @Override
  public CommandManager getImplementation() {
    return this;
  }

  public Set<String> getExtensions() {
    return EXT;
  }

  public ShellCommand resolveCommand(String name, byte[] source) throws CommandCreationException, NullPointerException {

    String script = new String(source);
    Compiler compiler = new Compiler();
    List<JavaClassFileObject> classes;
    try {
      classes = compiler.compile(name, script);
    }
    catch (IOException e) {
      throw new CommandCreationException(name);
    }
    catch (CompilationFailureException e) {
      throw new CommandCreationException(name);
    }
    if (classes.size() < 1) {
      throw new CommandCreationException(name);
    } else {
      JavaClassFileObject classFile = classes.get(0);
      LoadingClassLoader loader = new LoadingClassLoader(getContext().getLoader(), classes);
      try {
        Class<?> clazz = loader.loadClass(classFile.getClassName());
        return new BaseShellCommand(clazz);
      }
      catch (ClassNotFoundException e) {
        throw new CommandCreationException("Command " + name + " not found");
      }
    }
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
