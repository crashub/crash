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

//import crash.commands.base.system;
import org.crsh.auth.AuthInfo;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.lang.LanguageCommandResolver;
import org.crsh.lang.spi.Language;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandResolver;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.impl.command.system.SystemResolver;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CRaSH {

  /** . */
  final PluginContext context;

  /** . */
  final LanguageCommandResolver scriptResolver;

  /** . */
  private final ArrayList<CommandResolver> safeResolvers = new ArrayList<CommandResolver>();
  private final ArrayList<CommandResolver> semiSafeResolvers = new ArrayList<CommandResolver>();
  private final ArrayList<CommandResolver> unSafeResolvers = new ArrayList<CommandResolver>();

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
    for (CommandResolver resolver : context.getPlugins(CommandResolver.class)) {
      unSafeResolvers.add(resolver);
    }
    for (Language lang : context.getPlugins(Language.class)) {
      if (lang.isActive()) {
        langs.add(lang);
      }
    }

    unSafeResolvers.add(scriptResolver);
    unSafeResolvers.add(SystemResolver.UNSAFE_INSTANCE);
    unSafeResolvers.add(ExternalResolver.INSTANCE);
    safeResolvers.add(SystemResolver.SAFE_INSTANCE);
    safeResolvers.add(ExternalResolver.INSTANCE);
    semiSafeResolvers.add(SystemResolver.SEMI_SAFE_INSTANCE);
    semiSafeResolvers.add(ExternalResolver.INSTANCE);
  }

  public CRaSHSession createSession(Principal user, AuthInfo authInfo, ShellSafety shellSafety) {
    return new CRaSHSession(this, user, authInfo, shellSafety);
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
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.shell.impl.command.spi.CommandException if an error occurred preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public Command<?> getCommand(String name) throws CommandException, NullPointerException {
    return getCommandSafetyCheck(name, ShellSafetyFactory.getCurrentThreadShellSafety());
  }
  public Command<?> getCommandSafetyCheck(String name, ShellSafety shellSafety) throws CommandException, NullPointerException {
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }

    boolean safe = shellSafety.isSafeShell();
    boolean permitExit = shellSafety.permitExit();
    ArrayList<CommandResolver> resolvers = safe ? (permitExit ? semiSafeResolvers : safeResolvers) : unSafeResolvers;

    for (int i = 0;i < resolvers.size();i++) {
      Command<?> command = resolvers.get(i).resolveCommand(name, shellSafety);
      if (command != null) {
        return command;
      }
    }
    return null;
  }

  public Iterable<Map.Entry<String, String>> getCommands() {
    return getCommandsSafetyCheck(ShellSafetyFactory.getCurrentThreadShellSafety());
  }

  public Iterable<Map.Entry<String, String>> getCommandsSafetyCheck(ShellSafety shellSafety) {
    boolean safe = shellSafety.isSafeShell();
    boolean permitExit = shellSafety.permitExit();
    ArrayList<CommandResolver> resolvers = safe ? (permitExit ? semiSafeResolvers : safeResolvers) : unSafeResolvers;
    LinkedHashMap<String, String> names = new LinkedHashMap<String, String>();
    for (int i = 0;i < resolvers.size();i++) {
      for (Map.Entry<String, String> entry : resolvers.get(i).getDescriptions()) {
        names.put(entry.getKey(), entry.getValue());
      }
    }
    return names.entrySet();
  }
}
