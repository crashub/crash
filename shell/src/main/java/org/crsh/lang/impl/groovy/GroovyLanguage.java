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

import groovy.lang.GroovySystem;
import org.crsh.lang.spi.Language;
import org.crsh.lang.spi.Repl;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.impl.command.ShellSession;

/**
 * @author Julien Viet
 */
public class GroovyLanguage implements Language {

  /** . */
  private GroovyRepl repl;

  /** . */
  private GroovyCompiler compiler;

  public GroovyLanguage(PluginContext context) {
    compiler = new GroovyCompiler(context);
    repl = new GroovyRepl(this);
  }

  public String getName() {
    return "groovy";
  }

  @Override
  public String getDisplayName() {
    return "Groovy " + GroovySystem.getVersion();
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public Repl getRepl() {
    return repl;
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
