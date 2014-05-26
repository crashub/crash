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

import groovy.lang.Binding;
import groovy.lang.GroovySystem;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.lang.impl.groovy.command.GroovyScript;
import org.crsh.lang.spi.Language;
import org.crsh.lang.spi.Repl;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.util.ClassCache;
import org.crsh.util.TimestampedObject;

/**
 * @author Julien Viet
 */
public class GroovyLanguage implements Language {

  /** . */
  private ClassCache<GroovyScript> scriptCache;

  /** . */
  private GroovyRepl repl;

  /** . */
  private GroovyCompiler compiler;

  public GroovyLanguage(PluginContext context) {
    compiler = new GroovyCompiler(context);
    repl = new GroovyRepl(this);
    scriptCache = new ClassCache<GroovyScript>(context, new GroovyClassFactory<GroovyScript>(context.getLoader(), GroovyScript.class, GroovyScript.class), ResourceKind.LIFECYCLE);
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
    try {
      GroovyScript login = getLifeCycle(session, "login");
      if (login != null) {
        login.setBinding(new Binding(session));
        login.run();
      }
    }
    catch (CommandException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void destroy(ShellSession session) {
    try {
      GroovyScript logout = getLifeCycle(session, "logout");
      if (logout != null) {
        logout.setBinding(new Binding(session));
        logout.run();
      }
    }
    catch (CommandException e) {
      e.printStackTrace();
    }
  }

  public GroovyScript getLifeCycle(ShellSession session, String name) throws CommandException, NullPointerException {
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

}
