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

package crash.commands.test

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage

public class evaluate {

  @Command
  @Usage("evaluate groovy script")
  public void main(@Usage("the code") @Argument String scriptText) {
    Map<String, Object> state = [out:out];
    Binding binding = new Binding(state);
    GroovyShell shell = new GroovyShell(binding);
    shell.evaluate(scriptText);
  }
}