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

package org.crsh.command;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.logging.Logger;

public abstract class BaseCommand extends AbstractCommand {

  /** . */
  final Logger log = Logger.getLogger(getClass().getName());

  /** The unmatched text, only valid during an invocation. */
  protected String unmatched;

  protected BaseCommand() {
    this.unmatched = null;
  }

  protected final String readLine(String msg) {
    return readLine(msg, true);
  }

  protected final String readLine(String msg, boolean echo) {
    if (context instanceof InvocationContext) {
      return ((InvocationContext)context).readLine(msg, echo);
    } else {
      throw new IllegalStateException("Cannot invoke read line without an invocation context");
    }
  }

  public final String getUnmatched() {
    return unmatched;
  }


  public final void execute(String s) throws ScriptException, IOException {
    InvocationContext<?> context = peekContext();
    CommandInvoker invoker = context.resolve(s);
    invoker.open(context);
    invoker.flush();
    invoker.close();
  }

  public UndeclaredThrowableException toScript(Throwable cause) {
    return new UndeclaredThrowableException(cause);
  }
}
