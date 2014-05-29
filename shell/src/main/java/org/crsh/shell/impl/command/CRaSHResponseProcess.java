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
package org.crsh.shell.impl.command;

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.io.IOException;

/**
* @author Julien Viet
*/
class CRaSHResponseProcess extends CRaSHProcess {

  /** . */
  private final StringBuilder msg;

  /** . */
  private final ShellResponse response;

  public CRaSHResponseProcess(CRaSHSession session, String request, StringBuilder msg, ShellResponse response) {
    super(session, request);

    //
    this.msg = msg;
    this.response = response;
  }

  @Override
  ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {
    if (msg.length() > 0) {
      try {
        context.append(msg);
      }
      catch (IOException ignore) {
      }
    }
    return response;
  }
}
