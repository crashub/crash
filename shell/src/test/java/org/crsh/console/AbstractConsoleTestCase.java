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
package org.crsh.console;

import org.crsh.AbstractTestCase;
import test.shell.sync.SyncShell;

/**
 * @author Julien Viet
 */
public abstract class AbstractConsoleTestCase extends AbstractTestCase {

  /** . */
  protected String prompt;

  /** . */
  protected SyncShell shell;

  /** . */
  protected TestDriver driver;

  /** . */
  protected Console console;

  @Override
  protected void setUp() throws Exception {
    prompt = "";
    console = new Console(
        shell = new SyncShell() {
          @Override
          public String getPrompt() {
            return prompt;
          }
        },
        driver = new TestDriver());
  }

  protected final String getCurrentLine() {
    return ((Editor)console.handler.get()).getCurrentLine();
  }

  protected final int getCurrentCursor() {
    return ((Editor)console.handler.get()).getCurrentPosition();
  }

  protected final String getClipboard() {
    return ((Editor)console.handler.get()).getKillBuffer();
  }

  protected final void setClipboard(String s) {
    ((Editor)console.handler.get()).setKillBuffer(s);
  }
}
