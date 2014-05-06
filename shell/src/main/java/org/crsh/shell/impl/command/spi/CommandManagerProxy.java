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
package org.crsh.shell.impl.command.spi;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A command manager that is able to load a command manager via reflection.
 *
 * @author Julien Viet
 */
public class CommandManagerProxy extends CRaSHPlugin<CommandManager> implements CommandManager {

  /** . */
  private final AtomicReference<CommandManager> real = new AtomicReference<CommandManager>();

  /** . */
  private final String name;

  /** . */
  private final String className;

  /** . */
  private final Set<String> ext;

  public CommandManagerProxy(String name, String className, Set<String> ext) {
    this.name = name;
    this.className = className;
    this.ext = ext;
  }

  @Override
  public CommandManager getImplementation() {
    return this;
  }

  @Override
  public void init() {
    try {
      Class<CommandManager> mgrClass = (Class<CommandManager>)CommandManagerProxy.class.getClassLoader().loadClass(className);
      Constructor<CommandManager> mgrCtor = mgrClass.getConstructor(PluginContext.class);
      CommandManager mgr = mgrCtor.newInstance(getContext());
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
  public boolean isActive() {
    return real.get() != null;
  }

  @Override
  public Set<String> getExtensions() {
    return ext;
  }

  @Override
  public CommandResolution resolveCommand(String name, byte[] source) throws CommandCreationException, NullPointerException {
    CommandManager mgr = real.get();
    if (mgr != null) {
      return mgr.resolveCommand(name, source);
    } else {
      throw new IllegalStateException(name + " command manager is not available");
    }
  }

  @Override
  public void init(HashMap<String, Object> session) {
    CommandManager mgr = real.get();
    if (mgr != null) {
      mgr.init(session);
    } else {
      throw new IllegalStateException(name + " command manager is not available");
    }
  }

  @Override
  public void destroy(HashMap<String, Object> session) {
    CommandManager mgr = real.get();
    if (mgr != null) {
      mgr.destroy(session);
    } else {
      throw new IllegalStateException(name + " command manager is not available");
    }
  }

  @Override
  public String doCallBack(HashMap<String, Object> session, String name, String defaultValue) {
    CommandManager mgr = real.get();
    if (mgr != null) {
      return mgr.doCallBack(session, name, defaultValue);
    } else {
      throw new IllegalStateException(name + " command manager is not available");
    }
  }
}
