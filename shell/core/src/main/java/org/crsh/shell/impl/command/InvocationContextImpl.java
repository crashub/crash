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

import org.crsh.command.impl.BaseInvocationContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.ui.UIPrinterWriter;
import org.crsh.text.ShellAppendable;
import org.crsh.text.ShellPrintWriter;

import java.io.Flushable;
import java.util.Map;

class InvocationContextImpl<C, P> extends BaseInvocationContext<C, P> {

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private ShellPrintWriter writer;

  /** . */
  private ShellAppendable appendable;

  /** . */
  private final Flushable flushable;

  public InvocationContextImpl(
    ShellProcessContext processContext,
    ShellAppendable appendable,
    Flushable flushable,
    Iterable<C> consumedItems,
    Map<String, Object> session,
    Map<String, Object> attributes) {
    super(consumedItems, session, attributes);

    //
    this.writer = null;
    this.appendable = appendable;
    this.flushable = flushable;
    this.processContext = processContext;
  }

  public int getWidth() {
    return processContext.getWidth();
  }

  public String getProperty(String propertyName) {
    return processContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return processContext.readLine(msg, echo);
  }

  public ShellPrintWriter getWriter() {
    if (writer == null) {
      writer = new UIPrinterWriter(appendable, flushable, null, this);
    }
    return writer;
  }
}
