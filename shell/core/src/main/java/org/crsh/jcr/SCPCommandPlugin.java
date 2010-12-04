/*
 * Copyright (C) 2010 eXo Platform SAS.
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
import org.crsh.term.sshd.FailCommand;
import org.crsh.term.sshd.scp.CommandPlugin;
import org.crsh.term.sshd.scp.SCPAction;
import org.crsh.term.sshd.scp.SinkCommand;
import org.crsh.term.sshd.scp.SourceCommand;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SCPCommandPlugin extends CommandPlugin {

  @Override
  public Command createCommand(String command) {
    //
    if (command.startsWith("scp ")) {
      try {
        command = command.substring(4);
        SCPAction action = new SCPAction();
        CmdLineParser parser = new CmdLineParser(action);
        parser.parseArgument(command.split("(\\s)+"));
        if (Boolean.TRUE.equals(action.isSource())) {
          return new SourceCommand(action.getArgument(), Boolean.TRUE.equals(action.isRecursive()));
        }
        else if (Boolean.TRUE.equals(action.isSink())) {
          return new SinkCommand(action.getArgument(), Boolean.TRUE.equals(action.isRecursive()));
        }
        else {
          return new FailCommand("No handle that kind of action for now " + action);
        }
      }
      catch (CmdLineException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }
}
