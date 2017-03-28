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
package org.crsh.lang.impl.java;

import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.lang.spi.CommandResolution;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** @author Julien Viet */
public class JavaCompiler implements org.crsh.lang.spi.Compiler {

  /** . */
  private static final Set<String> EXT = Collections.singleton("java");

  /** . */
  private final org.crsh.lang.impl.java.Compiler compiler;

  /** . */
  private final ClassLoader loader;

  JavaCompiler(ClassLoader loader) {
    this.compiler = new org.crsh.lang.impl.java.Compiler(loader);
    this.loader = loader;
  }

  public Set<String> getExtensions() {
    return EXT;
  }

  public CommandResolution compileCommand(String name, byte[] source) throws CommandException, NullPointerException {
    String script = new String(source);
    List<JavaClassFileObject> classFiles;
    try {
      classFiles = compiler.compile(name, script);
    }
    catch (IOException e) {
      throw new CommandException(ErrorKind.INTERNAL, "Could not access command", e);
    }
    catch (CompilationFailureException e) {
        throw new CommandException(ErrorKind.INTERNAL, "Could not compile command: " + e.getMessage(), e);
    }
    for (JavaClassFileObject classFile : classFiles) {
      String className = classFile.getClassName();
      String simpleName = className.substring(className.lastIndexOf('.') + 1);
      if (simpleName.equals(name)) {
        LoadingClassLoader loader = new LoadingClassLoader(this.loader, classFiles);
        try {
          Class<?> clazz = loader.loadClass(classFile.getClassName());
          final ClassShellCommand command;
          try {
            command = new ClassShellCommand(clazz);
          }
          catch (IntrospectionException e) {
            throw new CommandException(ErrorKind.INTERNAL, "Invalid cli annotations", e);
          }
          final String description = command.describe(name, Format.DESCRIBE);
          return new CommandResolution() {
            @Override
            public String getDescription() {
              return description;
            }
            @Override
            public Command<Object> getCommand() throws CommandException {
              return command;
            }
          };
        }
        catch (ClassNotFoundException e) {
          throw new CommandException(ErrorKind.INTERNAL, "Command cannot be loaded", e);
        }
      }
    }
    throw new CommandException(ErrorKind.INTERNAL, "Command class not found");
  }

  public void init(ShellSession session) {
    //
  }

  public void destroy(ShellSession session) {
    //
  }

  public String doCallBack(ShellSession session, String name, String defaultValue) {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
