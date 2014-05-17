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
package org.crsh.shell;

import org.crsh.cli.Command;
import org.crsh.command.BaseCommand;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.spi.CommandCreationException;
import org.crsh.shell.impl.command.spi.CommandResolution;
import org.crsh.shell.impl.command.spi.ShellCommand;
import org.crsh.shell.impl.command.spi.ShellCommandResolver;
import org.crsh.util.Utils;

import java.util.Collections;
import java.util.List;

/**
 * @author Julien Viet
 */
public class CustomShellCommandResolverTestCase extends AbstractCommandTestCase {

  public static class mycommand extends BaseCommand {
    @Command
    public String main() {
      return "ok_mycommand";
    }
  }

  static class CustomShellCommandResolver extends CRaSHPlugin<ShellCommandResolver> implements ShellCommandResolver {

    @Override
    public ShellCommandResolver getImplementation() {
      return this;
    }

    @Override
    public Iterable<String> getCommandNames() {
      return Collections.singleton("mycommand");
    }

    @Override
    public CommandResolution resolveCommand(String name) throws CommandCreationException, NullPointerException {
      if ("mycommand".equals(name)) {
        return new CommandResolution() {
          @Override
          public String getDescription() {
            return "mycommand";
          }
          @Override
          public ShellCommand<?> getCommand() throws CommandCreationException {
            return new ClassShellCommand<mycommand>(mycommand.class);
          }
        };
      }
      return null;
    }
  }

  @Override
  protected List<CRaSHPlugin<?>> getPlugins() {
    List<CRaSHPlugin<?>> plugins = super.getPlugins();
    plugins.add(new CustomShellCommandResolver());
    return plugins;
  }

  public void testFoo() {
    assertEquals("ok_mycommand", assertOk("mycommand"));
    List<String> names = Utils.list(session.getCommandNames());
    assertTrue("Was expecting " + names + " to contain mycommand", names.contains("mycommand"));
  }
}
