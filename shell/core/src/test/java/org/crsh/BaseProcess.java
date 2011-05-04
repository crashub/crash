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
package org.crsh;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

/**
 * A process that does nothing.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class BaseProcess implements ShellProcess {

  /** . */
  private ShellProcessContext processContext;

  public void process(String request, ShellProcessContext processContext) {
    processContext.begin(this);
    this.processContext = processContext;
    try {
      ShellResponse resp = execute(request);
      processContext.end(resp);
    } finally {
      this.processContext = null;
    }
  }

  protected final String readLine(String msg, boolean echo) {
    return processContext.readLine(msg, echo);
  }

  protected ShellResponse execute(String request) {
    return new ShellResponse.Ok();
  }

  public void cancel() {
  }
}
