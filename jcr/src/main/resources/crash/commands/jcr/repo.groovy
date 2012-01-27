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

import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.JCR
import org.crsh.jcr.command.InitProperties
import org.crsh.shell.ui.UIBuilder

@Usage("repository interaction commands")
class repo extends org.crsh.jcr.command.JCRCommand {

  @Usage("changes the current repository")
  @Man("""
  The use command changes the current repository used by for JCR commands. The command must at least
  have a URL parameter to be used in connecting to the repository.

  For the eXo repository you can use a container bound repository:
  % repo use container=portal

  For the jackrabbit implementation you can use a JNDI bound repository:
  % repo use org.apache.jackrabbit.repository.uri=rmi://localhost:1099/jackrabbit

  More generally:
  % repo use parameterName=parameterValue;nextParameterName=nextParameterValue
""")
  @Command
  public Object use(
      @Argument
      @Usage("the parameters")
      @Man("The parameters used to instantiate the repository to be used in this session") InitProperties parameters) throws ScriptException {
    repository = JCR.getRepository(parameters.getProperties());
    return info();
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
