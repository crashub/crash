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
package org.crsh.shell.impl.command;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.shell.impl.command.spi.CommandManager;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.ShellCommandResolver;
import org.crsh.util.TimestampedObject;
import org.crsh.vfs.Resource;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Julien Viet
 */
public class ScriptResolver implements ShellCommandResolver {

  /** . */
  private final Map<String, TimestampedObject<CommandResolution>> commandCache = new ConcurrentHashMap<String, TimestampedObject<CommandResolution>>();

  /** . */
  final HashMap<String, CommandManager> activeManagers = new HashMap<String, CommandManager>();

  /** . */
  final PluginContext context;

  public ScriptResolver(PluginContext context) {

    //
    for (CommandManager manager : context.getPlugins(CommandManager.class)) {
      if (manager.isActive()) {
        for (String ext : manager.getExtensions()) {
          activeManagers.put(ext, manager);
        }
      }
    }

    this.context = context;
  }

  @Override
  public Iterable<String> getCommandNames() {
    // Todo change to a live iteration...
    LinkedHashSet<String> names = new LinkedHashSet<String>();
    for (String resourceName : context.listResources(ResourceKind.COMMAND)) {
      int index = resourceName.indexOf('.');
      String name = resourceName.substring(0, index);
      String ext = resourceName.substring(index + 1);
      if (activeManagers.containsKey(ext)) {
        names.add(name);
      }
    }
    return names;
  }

  @Override
  public CommandResolution resolveCommand(String name) throws CommandCreationException, NullPointerException {
    for (CommandManager manager : activeManagers.values()) {
      if (manager.isActive()) {
        for (String ext : manager.getExtensions()) {
          Iterable<Resource> resources = context.loadResources(name + "." + ext, ResourceKind.COMMAND);
          for (Resource resource : resources) {
            CommandResolution resolution = resolveCommand(manager, name, resource);
            if (resolution != null) {
              return resolution;
            }
          }
        }
      }
    }
    return null;
  }

  private CommandResolution resolveCommand(CommandManager manager, String name, Resource script) throws CommandCreationException {
    TimestampedObject<CommandResolution> ref = commandCache.get(name);
    if (ref != null) {
      if (script.getTimestamp() != ref.getTimestamp()) {
        ref = null;
      }
    }
    CommandResolution command;
    if (ref == null) {
      command = manager.resolveCommand(name, script.getContent());
      if (command != null) {
        commandCache.put(name, new TimestampedObject<CommandResolution>(script.getTimestamp(), command));
      }
    } else {
      command = ref.getObject();
    }
    return command;
  }

}
