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

package org.crsh.doc;

import org.crsh.command.DescriptionMode;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;
import org.crsh.shell.impl.CRaSH;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
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
    FS fs = new FS();
    fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
    PluginContext ctx = new PluginContext(fs, Thread.currentThread().getContextClassLoader());
    ctx.refresh();
    CRaSH crash = new CRaSH(ctx);
    for (String s : ctx.listResourceId(ResourceKind.SCRIPT)) {
      ShellCommand cmd = crash.getCommand(s);
      String man = cmd.describe(s, DescriptionMode.MAN);
      if (man != null) {
        File f = new File(root, s + ".wiki");
        if (!f.exists()) {
          PrintWriter pw = new PrintWriter(f);
          try {
            System.out.println("Generating wiki file " + f.getCanonicalPath());
            pw.print("{{screen}}" + man + "{{/screen}}");
          } finally {
            pw.close();
          }
        }
      }
    }
  }
}
