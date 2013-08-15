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
package org.crsh.lang.groovy.closure;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.command.CommandCreationException;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.command.pipeline.PipeLine;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
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

  public void invoke(InvocationContext<Object> context) throws IOException, UndeclaredThrowableException {

    //
    PipeLineInvocationContext inner = new PipeLineInvocationContext(context, false);
    LinkedList<CommandInvoker> pipe;
    try {
      pipe = closure.resolve2(args);
    }
    catch (CommandCreationException e) {
      throw new UndeclaredThrowableException(e);
    }
    CommandInvoker[] array = pipe.toArray(new CommandInvoker[pipe.size()]);
    PipeLine pipeLine = new PipeLine(array);

    //
    try {
      pipeLine.open(inner);
      pipeLine.flush();

    }
    catch (ScriptException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        throw new InvokerInvocationException(cause);
      } else {
        throw e;
      }
    } finally {
      // This is on purpose
      pipeLine.close();
    }
  }
}
