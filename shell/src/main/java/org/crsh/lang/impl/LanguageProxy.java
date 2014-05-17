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
package org.crsh.lang.impl;

import org.crsh.lang.spi.Compiler;
import org.crsh.lang.spi.Language;
import org.crsh.lang.spi.Repl;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.impl.command.ShellSession;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A command manager that is able to load a command manager via reflection.
 *
 * @author Julien Viet
 */
public class LanguageProxy extends CRaSHPlugin<Language> implements Language {

  /** . */
  private final AtomicReference<Language> real = new AtomicReference<Language>();

  /** . */
  private final String name;

  /** . */
  private final String className;

  public LanguageProxy(String name, String className) {
    this.name = name;
    this.className = className;
  }

  @Override
  public Language getImplementation() {
    return this;
  }

  @Override
  public void init() {
    try {
      Class<Language> mgrClass = (Class<Language>)getClass().getClassLoader().loadClass(className);
      Constructor<Language> mgrCtor = mgrClass.getConstructor(PluginContext.class);
      Language mgr = mgrCtor.newInstance(getContext());
      real.set(mgr);
    }
    catch (Exception e) {
      log.info("Plugin is inactive");
    }
    catch (NoClassDefFoundError e) {
      log.info("Plugin is inactive");
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return real.get() != null ? real.get().getDisplayName() : "";
  }

  @Override
  public boolean isActive() {
    return real.get() != null;
  }

  @Override
  public Repl getRepl() {
    Language lang = real.get();
    if (lang != null) {
      return lang.getRepl();
    } else {
      throw new IllegalStateException(name + " language is not available");
    }
  }

  @Override
  public Compiler getCompiler() {
    Language lang = real.get();
    if (lang != null) {
      return lang.getCompiler();
    } else {
      throw new IllegalStateException(name + " language is not available");
    }
  }

  @Override
  public void init(ShellSession session) {
    Language lang = real.get();
    if (lang != null) {
      lang.init(session);
    } else {
      throw new IllegalStateException(name + " language is not available");
    }
  }

  @Override
  public void destroy(ShellSession session) {
    Language lang = real.get();
    if (lang != null) {
      lang.destroy(session);
    } else {
      throw new IllegalStateException(name + " language is not available");
    }
  }
}
