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
package org.crsh.lang;

import org.crsh.lang.impl.script.ScriptCompiler;
import org.crsh.lang.spi.Compiler;
import org.crsh.lang.spi.Language;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandResolver;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.util.TimestampedObject;
import org.crsh.vfs.Resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A shell command resolver for languages.
 *
 * @author Julien Viet
 */
public class LanguageCommandResolver implements CommandResolver {

  /** . */
  private final Map<String, TimestampedObject<CommandResolution>> commandCache = new ConcurrentHashMap<String, TimestampedObject<CommandResolution>>();

  /** . */
  final HashMap<String, Compiler> activeCompilers = new HashMap<String, Compiler>();

  /** . */
  final PluginContext context;

  public LanguageCommandResolver(PluginContext context) {

    //
    activeCompilers.put("script", ScriptCompiler.getInstance());

    //
    for (Language lang : context.getPlugins(Language.class)) {
      if (lang.isActive()) {
        Compiler compiler = lang.getCompiler();
        if (compiler != null) {
          for (String ext : compiler.getExtensions()) {
            activeCompilers.put(ext, compiler);
          }
        }
      }
    }

    this.context = context;
  }

  public Compiler getCompiler(String name) {
    return activeCompilers.get(name);
  }

  @Override
  public Iterable<Map.Entry<String, String>> getDescriptions() {
    LinkedHashMap<String, String> commands = new LinkedHashMap<String, String>();
    for (String resourceName : context.listResources(ResourceKind.COMMAND)) {
      int index = resourceName.indexOf('.');
      String name = resourceName.substring(0, index);
      String ext = resourceName.substring(index + 1);
      if (activeCompilers.containsKey(ext)) {
        try {
          CommandResolution resolution = resolveCommand2(name);
          commands.put(name, resolution.getDescription());
        }
        catch (CommandException e) {
          //
        }
      }
    }
    return commands.entrySet();
  }

  @Override
  public Command<?> resolveCommand(String name) throws CommandException, NullPointerException {
    CommandResolution resolution = resolveCommand2(name);
    return resolution != null ? resolution.getCommand() : null;
  }

  private CommandResolution resolveCommand2(String name) throws CommandException, NullPointerException {
    for (Compiler manager : activeCompilers.values()) {
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
    return null;
  }

  private CommandResolution resolveCommand(org.crsh.lang.spi.Compiler manager, String name, Resource script) throws CommandException {
    TimestampedObject<CommandResolution> ref = commandCache.get(name);
    if (ref != null) {
      if (script.getTimestamp() != ref.getTimestamp()) {
        ref = null;
      }
    }
    CommandResolution command;
    if (ref == null) {
      command = manager.compileCommand(name, script.getContent());
      if (command != null) {
        commandCache.put(name, new TimestampedObject<CommandResolution>(script.getTimestamp(), command));
      }
    } else {
      command = ref.getObject();
    }
    return command;
  }

}
