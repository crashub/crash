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

package org.crsh.doc;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.DescriptionFormat;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;

public class Generator {

  public static void main(String[] args) throws Exception {
    File root = new File(args[0]);
    if (!root.exists()) {
      if (!root.mkdirs()) {
        throw new AssertionError("Could not create root directory " + root.getCanonicalPath());
      }
    } else {
      if (!root.isDirectory()) {
        throw new IllegalArgumentException("Wrong root directory argument " + root.getCanonicalPath());
      }
    }
    FS cmdFS = new FS().mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/commands/"));
    FS confFS = new FS().mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
    PluginContext ctx = new PluginContext(
      new ServiceLoaderDiscovery(Thread.currentThread().getContextClassLoader()),
      Collections.<String, Object>emptyMap(),
      cmdFS,
      confFS,
      Thread.currentThread().getContextClassLoader());
    ctx.refresh();
    CRaSH crash = new CRaSH(ctx);
    for (String s : ctx.listResourceId(ResourceKind.COMMAND)) {
      ShellCommand cmd = crash.getCommand(s);
      StringBuilder man = new StringBuilder();
      if (cmd instanceof CRaSHCommand) {
        CRaSHCommand cc = (CRaSHCommand)cmd;
        CommandDescriptor<?> desc = cc.getDescriptor();
        if (desc.getSubordinates().size() > 1) {
          for (CommandDescriptor<?> m : desc.getSubordinates().values()) {
            man.append("{{screen}}");
            m.printMan(man);
            man.append("{{/screen}}");
          }
        } else {
          man.append("{{screen}}");
          desc.printMan(man);
          man.append("{{/screen}}");
        }
      } else {
        man.append(cmd.describe(s, DescriptionFormat.MAN));
      }
      if (man.length() > 0) {
        File f = new File(root, s + ".wiki");
        if (!f.exists()) {
          PrintWriter pw = new PrintWriter(f);
          try {
            System.out.println("Generating wiki file " + f.getCanonicalPath());
            pw.print(man);
          } finally {
            pw.close();
          }
        }
      }
    }
  }
}
