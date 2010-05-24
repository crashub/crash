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

import org.crsh.command.ShellCommand;
import org.crsh.display.DisplayBuilder;
import org.crsh.util.CompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Evaluable implements Callable<ShellResponse> {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private final CRaSH shell;

  /** . */
  private final String s;

  /** . */
  private final CompletionHandler<ShellResponse> handler;

  public Evaluable(CRaSH shell, String s, CompletionHandler<ShellResponse> handler) {
    this.shell = shell;
    this.s = s;
    this.handler = handler;
  }

  public ShellResponse call() {

    // Trim
    String s2 = s.trim();

    //
    log.debug("Invoking command " + s2);

    //
    ShellResponse response;
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
            response = new ShellResponse.Display(((DisplayBuilder) o).getElements());
          } else if (o != null) {
            response = new ShellResponse.Display(o.toString());
          } else {
            response = new ShellResponse.Ok();
          }

        } else {
          response = new ShellResponse.UnkownCommand(chunks.get(0));
        }
      }
      catch (Throwable t) {
        response = new ShellResponse.Error(ErrorType.EVALUATION, t);
      }
    } else {
      response = new ShellResponse.NoCommand();
    }

    //
    log.debug("Making handler response callback");

    //
    if (handler != null) {
      handler.completed(response);
    }

    //
    return response;
  }
}
