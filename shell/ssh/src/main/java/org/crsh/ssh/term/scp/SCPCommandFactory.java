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
package org.crsh.ssh.term.scp;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.crsh.plugin.PluginContext;
import org.crsh.ssh.term.FailCommand;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SCPCommandFactory implements CommandFactory {

  /** . */
  private static final Logger log = Logger.getLogger(SCPCommandFactory.class.getName());

  /** . */
  private final PluginContext pluginContext;

  public SCPCommandFactory(PluginContext pluginContext) {
    this.pluginContext = pluginContext;
  }

  public Command createCommand(String command) {
    // Just in case
    command = command.trim();

    //
    log.log(Level.FINE, "About to execute shell command " + command);

    for (CommandPlugin plugin : pluginContext.getPlugins(CommandPlugin.class)) {
      Command cmd = plugin.createCommand(command);
      if (cmd != null) {
        return cmd;
      }
    }

    return new FailCommand("Unrecognized command " + command);
  }
}
