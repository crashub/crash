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

package org.crsh.jcr;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.apache.sshd.server.Command;
import org.crsh.cli.CLIException;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.invocation.Resolver;
import org.crsh.ssh.term.FailCommand;
import org.crsh.ssh.term.scp.CommandPlugin;
import org.crsh.ssh.term.scp.SCPAction;

public class SCPCommandPlugin extends CommandPlugin {

  @Override
  public Command createCommand(String command) {
    //
    if (command.startsWith("scp ")) {
      try {
        command = command.substring(4);
        SCPAction action = new SCPAction();
        CommandDescriptor<SCPAction> descriptor = CommandFactory.DEFAULT.create(SCPAction.class);
        InvocationMatcher<SCPAction> analyzer = descriptor.matcher("main");
        InvocationMatch<SCPAction> match = analyzer.parse(command);
        match.invoke(Resolver.EMPTY, action);
        if (Boolean.TRUE.equals(action.isSource())) {
          return new SourceCommand(action.getTarget(), Boolean.TRUE.equals(action.isRecursive()));
        }
        else if (Boolean.TRUE.equals(action.isSink())) {
          return new SinkCommand(action.getTarget(), Boolean.TRUE.equals(action.isRecursive()));
        }
        else {
          return new FailCommand("Cannot execute command " + command);
        }
      }
      catch (CLIException e) {
        return new FailCommand("Cannot execute command " + command, e);
      }
    } else {
      return new FailCommand("Cannot execute command " + command);
    }
  }
}
