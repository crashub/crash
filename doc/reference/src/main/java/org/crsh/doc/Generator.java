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
import org.crsh.cli.descriptor.Format;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

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
    StringBuilder buffer = new StringBuilder();
    for (Map.Entry<String, String> s : crash.getCommands()) {
      Command<?> cmd = crash.getCommand(s.getKey());
      CommandDescriptor<?> desc = cmd.getDescriptor();
      buffer.append("== ").append(desc.getName()).append("\n").append("\n");
      buffer.append("----\n");
      buffer.append(cmd.describe("", Format.MAN));
      buffer.append("----\n");
      for (CommandDescriptor<?> m : desc.getSubordinates().values()) {
        buffer.append("=== ").append(desc.getName()).append(" ").append(m.getName()).append("\n").append("\n");
        buffer.append("----\n");
        buffer.append(cmd.describe(m.getName(), Format.MAN));
        buffer.append("----\n\n");
      }
    }
    if (buffer.length() > 0) {
      File f = new File(root, "reference.asciidoc");
      PrintWriter pw = new PrintWriter(f);
      try {
        System.out.println("Generating asciidoc file " + f.getCanonicalPath());
        pw.print(buffer);
      } finally {
        pw.close();
      }
    }
  }
}
