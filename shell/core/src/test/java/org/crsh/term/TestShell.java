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

package org.crsh.term;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestShell implements Shell {


  /** . */
  private final BlockingQueue<TestShellAction> queue;

  public TestShell() {
    queue = new ArrayBlockingQueue<TestShellAction>(10);
  }

  public int getSize() {
    return queue.size();
  }

  public void append(TestShellAction action) {
    queue.add(action);
  }

  public String getWelcome() {
    return "Welcome\r\n% ";
  }

  public List<String> complete(String prefix) {
    throw new UnsupportedOperationException();
  }

  public String getPrompt() {
    return "% ";
  }

  public void evaluate(String request, ShellResponseContext responseContext) {
    try {
      TestShellAction action = queue.take();
      ShellResponse resp = action.evaluate(request, responseContext);
      responseContext.done(resp);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public void doClose() {
  }
}