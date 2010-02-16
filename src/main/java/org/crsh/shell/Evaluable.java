/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.crsh.display.DisplayBuilder;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Evaluable implements Callable<ShellResponse> {

  /** . */
  private final Shell shell;

  /** . */
  private final String s;

  public Evaluable(Shell shell, String s) {
    this.shell = shell;
    this.s = s;
  }

  public ShellResponse evaluate() {
    try {
      return call();
    } catch (Exception e) {
      return new ShellResponse.Error(ErrorType.INTERNAL, e);
    }
  }

  public ShellResponse call() throws Exception {
    // Trim
    String s2 = s.trim();

    //
    if (s2.length() > 0) {
      try {
        // We'll have at least one chunk
        List<String> chunks = LineFormat.format(s2);

        // Get command
        ShellCommand cmd = shell.getClosure(chunks.get(0));

        //
        if (cmd != null) {
          // Build args
          String[] args = new String[chunks.size() - 1];
          chunks.subList(1, chunks.size()).toArray(args);
          Object o = cmd.execute(shell.commandContext, args);
          if (o instanceof DisplayBuilder) {
            return new ShellResponse.Display(((DisplayBuilder) o).getElements());
          } else if (o != null) {
            return new ShellResponse.Display(o.toString());
          } else {
            return new ShellResponse.Ok();
          }

        } else {
          return new ShellResponse.UnkownCommand(chunks.get(0));
        }
      }
      catch (Throwable t) {
        return new ShellResponse.Error(ErrorType.EVALUATION, t);
      }
    } else {
      return new ShellResponse.NoCommand();
    }
  }
}
