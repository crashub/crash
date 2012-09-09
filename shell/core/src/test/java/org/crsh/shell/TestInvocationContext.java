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

package org.crsh.shell;

import org.crsh.command.CommandInvoker;
import org.crsh.command.ShellCommand;
import org.crsh.command.impl.BaseInvocationContext;
import org.crsh.shell.io.ShellFormatter;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.text.ChunkBuffer;

import java.util.*;

public class TestInvocationContext<C, P> extends BaseInvocationContext<C, P> {

  /** . */
  protected ChunkBuffer reader;

  /** . */
  protected ShellPrinter writer;

  public TestInvocationContext() {
    super(Collections.<C>emptyList(), new HashMap<String, Object>(), new HashMap<String, Object>());

    //
    this.reader = null;
    this.writer = null;
  }

  public ChunkBuffer getReader() {
    return reader;
  }

  public int getWidth() {
    return 32;
  }

  public String getProperty(String propertyName) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    throw new UnsupportedOperationException();
  }

  public String execute(ShellCommand command, String... args) throws Exception {
    if (reader != null) {
      reader.clear();
    }
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(arg);
    }
    CommandInvoker<C, P> invoker = (CommandInvoker<C, P>)command.createInvoker(sb.toString());
    invoker.invoke(this);
    return reader != null ? reader.toString() : null;
  }

  public ShellPrinter getWriter() {
    if (writer == null) {
      reader = new ChunkBuffer();
      writer = new ShellPrinter(new ShellFormatter(reader, "\r\n"), null, null, this);
    }
    return writer;
  }
}
