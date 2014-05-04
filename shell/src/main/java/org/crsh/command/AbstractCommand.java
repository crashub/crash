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

import org.crsh.text.RenderPrintWriter;

import java.util.LinkedList;

public abstract class AbstractCommand {

  /** . */
  private LinkedList<InvocationContext<?>> stack;

  /** The current context : need to find a way to make not that public. */
  public RuntimeContext context;

  /** The current output. */
  protected RenderPrintWriter out;

  protected AbstractCommand() {
    this.stack = null;
    this.context = null;
  }

  public final void pushContext(InvocationContext<?> context) throws NullPointerException {
    if (context == null) {
      throw new NullPointerException();
    }

    //
    if (stack == null) {
      stack = new LinkedList<InvocationContext<?>>();
    }

    // Save current context (is null the first time)
    stack.addLast((InvocationContext<?>)this.context);

    // Set new context
    this.context = context;
    this.out = context.getWriter();
  }

  public final InvocationContext<?> popContext() {
    if (stack != null && stack.size() > 0) {
      InvocationContext context = (InvocationContext)this.context;
      this.context = stack.removeLast();
      this.out = this.context != null ? ((InvocationContext)this.context).getWriter() : null;
      return context;
    } else {
      return null;
    }
  }

  public final InvocationContext<?> peekContext() {
    return (InvocationContext<?>)context;
  }
}
