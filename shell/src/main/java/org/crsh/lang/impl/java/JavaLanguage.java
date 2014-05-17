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

import org.crsh.lang.spi.*;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.ShellSession;

/**
 * @author Julien Viet
 */
public class JavaLanguage extends CRaSHPlugin<Language> implements Language {

  /** . */
  private JavaCompiler compiler;

  @Override
  public void init() {
    compiler = new JavaCompiler(getContext().getLoader());
  }

  @Override
  public String getName() {
    return "java";
  }

  @Override
  public String getDisplayName() {
    return System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version");
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public Language getImplementation() {
    return this;
  }

  @Override
  public Repl getRepl() {
    return null;
  }

  @Override
  public org.crsh.lang.spi.Compiler getCompiler() {
    return compiler;
  }

  @Override
  public void init(ShellSession session) {
    compiler.init(session);
  }

  @Override
  public void destroy(ShellSession session) {
    compiler.destroy(session);
  }
}
