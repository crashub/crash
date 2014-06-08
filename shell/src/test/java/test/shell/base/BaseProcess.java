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
package test.shell.base;

import junit.framework.AssertionFailedError;
import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.io.IOException;

public class BaseProcess implements ShellProcess {

  /** . */
  private final String request;

  /** . */
  private ShellProcessContext processContext;

  public BaseProcess(String request) {
    this.request = request;
  }

  public void process(String request, ShellProcessContext processContext) throws IOException {
    this.processContext = processContext;
    try {
      ShellResponse resp = execute(request);
      processContext.end(resp);
    } finally {
      this.processContext = null;
    }
  }

  protected ShellResponse execute(String request) {
    return ShellResponse.ok();
  }

  @Override
  public KeyHandler getKeyHandler() {
    return null;
  }

  protected final String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    return processContext.readLine(msg, echo);
  }

  public final void execute(ShellProcessContext processContext) {
    this.processContext = processContext;
    try {
      process(request, processContext);
    }
    catch (IOException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public void cancel() {
  }
}
