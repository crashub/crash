/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Usage

import org.crsh.text.ui.UIBuilder
import org.crsh.jcr.JCRPlugin

@Usage("repository interaction commands")
@Man("""\
The repo commands allow to select a repository to use, it is the main entry point for using JCR commands.

The set of available repository plugins can be discovered with the ls command:

% repo ls
Available JCR plugins:
exo - Exo JCR plugin - You can use a container bound repository: 'repo use container=portal'

A repository can then be selected with the use command.

For the eXo repository you can use a container bound repository:
% repo use container=portal


jackrabbit - Jackrabbit JCR Plugin.

JackRabbit must be properly configured to provide any of the following access
 methods. For the details, see the following resources.
 * http://jackrabbit.apache.org/deployment-models.html
 * http://wiki.apache.org/jackrabbit/RemoteAccess
 * http://jackrabbit.apache.org/shared-j2ee-resource-howto.html

Connect via RMI:
% repo use org.apache.jackrabbit.repository.uri=rmi://localhost:1099/jackrabbit
Connect via JNDI:
% repo use org.apache.jackrabbit.repository.uri=jndi:java:comp/env/jcr/jackrabbit
Connect via WebDAV:
% repo use org.apache.jackrabbit.repository.uri=http://localhost:8080/jackrabbit-webapp/repository/
""")
class repo extends org.crsh.jcr.command.JCRCommand {

  @Usage("changes the current repository")
  @Man("""\
The use command changes the current repository used by for JCR commands. The command accepts a set of properties
as main command argument that will be used to select a repository:

% repo use parameterName=parameterValue;nextParameterName=nextParameterValue

The parameters is specific to JCR plugin implementations, more details can be found thanks to the ls command.
""")
  @Command
  public Object use(
      @Argument
      @Usage("the parameters")
      @Man("The parameters used to instantiate the repository to be used in this session") Properties parameters) {
    repository = JCRPlugin.findRepository(parameters);
    return info();
  }

  @Usage("list the available repository plugins")
  @Man("The ls command print the available repository plugins.")
  @Command
  public Object ls(){
    StringBuilder sb = new StringBuilder("Available JCR plugins:\n")
    JCRPlugin.findRepositories().each() { plugin ->
      sb.append("$plugin.name - $plugin.displayName - $plugin.usage\n");
    }
    return sb.toString();
  }

  @Usage("show info about the current repository")
  @Man("The info command print the descriptor of the current repository.")
  @Command
  public Object info() {
    if (repository == null) {
      throw new ScriptException("Not connected to a repository");
    }
    def builder = new UIBuilder();
    repository.descriptorKeys.each { key ->
      def val = repository.getDescriptor(key);
      if (val != null) {
        builder.node("$key : $val");
      }
    }
    return builder;
  }
}
