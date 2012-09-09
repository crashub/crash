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

import org.apache.sshd.server.Command;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.matcher.CmdLineException;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.InvocationContext;
import org.crsh.cmdline.matcher.Matcher;
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
        ClassDescriptor<SCPAction> descriptor = CommandFactory.create(SCPAction.class);
        Matcher<SCPAction> analyzer = Matcher.createMatcher("main", descriptor);
        CommandMatch<SCPAction, ?, ?> match = analyzer.match(command);
        match.invoke(new InvocationContext(), action);
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
      catch (CmdLineException e) {
        return new FailCommand("Cannot execute command " + command, e);
      }
      catch (IntrospectionException e) {
        return new FailCommand("Cannot execute command " + command, e);
      }
    } else {
      return new FailCommand("Cannot execute command " + command);
    }
  }
}
