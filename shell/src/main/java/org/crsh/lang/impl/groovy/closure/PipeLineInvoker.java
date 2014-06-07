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
package org.crsh.lang.impl.groovy.closure;

import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.shell.impl.command.pipeline.PipeLine;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/** @author Julien Viet */
public class PipeLineInvoker {

  /** . */
  private final PipeLineClosure closure;

  /** . */
  private final Object[] args;

  public PipeLineInvoker(PipeLineClosure closure, Object[] args) {
    this.closure = closure;
    this.args = args;
  }

  public void invoke(InvocationContext<Object> context) throws IOException, CommandException {

    //
    PipeLineInvocationContext inner = new PipeLineInvocationContext(context);
    LinkedList<CommandInvoker> pipe = closure.resolve2(args);
    CommandInvoker[] array = pipe.toArray(new CommandInvoker[pipe.size()]);
    PipeLine pipeLine = new PipeLine(array);

    //
    try {
      pipeLine.open(inner);
      pipeLine.flush();
    } finally {
      // This is on purpose
      pipeLine.close();
    }
  }

  @Override
  public String toString() {
    return "PipeLineInvoker[" + closure + "](" + Arrays.toString(args) + ")";
  }
}
