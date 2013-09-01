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

package org.crsh.ssh.term.inline;

import org.apache.sshd.server.Command;
import org.crsh.ssh.term.scp.CommandPlugin;

/***
 * SSH inline command plugin
 */
public class SSHInlinePlugin extends CommandPlugin {

    @Override
    public Command createCommand(String command) {

        if (!command.startsWith("scp ")) {
            return new SSHInlineCommand(command, getContext());
        }

        return null;
    }
}
