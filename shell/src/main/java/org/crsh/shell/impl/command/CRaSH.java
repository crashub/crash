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

import org.crsh.lang.LanguageCommandResolver;
import org.crsh.lang.spi.Language;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.ShellCommandResolver;
import org.crsh.shell.impl.command.system.SystemResolver;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CRaSH {

  /** . */
  final PluginContext context;

  /** . */
  final LanguageCommandResolver scriptResolver;

  /** . */
  private final ArrayList<ShellCommandResolver> resolvers = new ArrayList<ShellCommandResolver>();

  /** . */
  final ArrayList<Language> langs = new ArrayList<Language>();

  /**
   * Create a new CRaSH.
   *
   * @param context the plugin context
   * @throws NullPointerException if the context argument is null
   */
  public CRaSH(PluginContext context) throws NullPointerException {
    this.context = context;
    this.scriptResolver = new LanguageCommandResolver(context);

    // Add the resolver plugins
    for (ShellCommandResolver resolver : context.getPlugins(ShellCommandResolver.class)) {
      resolvers.add(resolver);
    }
    for (Language lang : context.getPlugins(Language.class)) {
      if (lang.isActive()) {
        langs.add(lang);
      }
    }

    //
    resolvers.add(scriptResolver);
    resolvers.add(SystemResolver.INSTANCE);
  }

  public CRaSHSession createSession(Principal user) {
    return new CRaSHSession(this, user);
  }

  /**
   * Returns the plugin context.
   *
   * @return the plugin context
   */
  public PluginContext getContext() {
    return context;
  }

  /**
   * Attempt to obtain a command description. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command description
   * @throws org.crsh.shell.impl.command.spi.CommandCreationException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public String getCommandDescription(String name) throws CommandCreationException, NullPointerException {
    CommandResolution resolution = resolveCommand(name);
    return resolution != null ? resolution.getDescription() : null;
  }

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.shell.impl.command.spi.CommandCreationException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand<?> getCommand(String name) throws CommandCreationException, NullPointerException {
    CommandResolution resolution = resolveCommand(name);
    return resolution != null ? resolution.getCommand() : null;
  }

  public CommandResolution resolveCommand(String name) throws CommandCreationException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    for (int i = 0;i < resolvers.size();i++) {
      CommandResolution resolution = resolvers.get(i).resolveCommand(name);
      if (resolution != null) {
        return resolution;
      }
    }
    return null;
  }

  public Iterable<String> getCommandNames() {
    LinkedHashSet<String> names = new LinkedHashSet<String>();
    for (int i = 0;i < resolvers.size();i++) {
      for (String s : resolvers.get(i).getCommandNames()) {
        names.add(s);
      }
    }
    return names;
  }
}
