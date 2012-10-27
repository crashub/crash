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

import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;

import java.io.IOException;

class PipeLine implements CommandInvoker {

  /** . */
  private final PipeFilter[] pipes;

  PipeLine(PipeFilter[] pipes) {
    this.pipes = pipes;
  }

  public void invoke(InvocationContext<?> context) throws ScriptException, IOException {
    open(context);
    flush();
    close();
  }

  public Class getConsumedType() {
    throw new UnsupportedOperationException();
  }

  public Class getProducedType() {
    throw new UnsupportedOperationException();
  }

  public void setPiped(boolean piped) {
    throw new UnsupportedOperationException("This should not be called");
  }

  public void open(InvocationContext context) {

    InvocationContext<?> last = context;

    for (int i = pipes.length - 1;i >= 0;i--) {

      //
      InvocationContext next;

      // Open the next
      // Try to do some type adaptation
      if (pipes[i].getProducedType() == Chunk.class) {
        if (last.getConsumedType() == Chunk.class) {
          next = last;
        } else {
          throw new UnsupportedOperationException("Not supported yet");
        }
      } else {
        if (last.getConsumedType().isAssignableFrom(pipes[i].getProducedType())) {
          next = last;
        } else {
          Foo foo = new Foo();
          foo.open(last);
          next = foo;
        }
      }

      //
      if (i > 0) {
        pipes[i].setPiped(true);
      }

      //
      pipes[i].open(next);


      //
      last = pipes[i];
    }
  }

  public void close() {
    pipes[0].close();
  }

  public void provide(Object element) throws IOException {
    pipes[0].provide(element);
  }

  public void flush() throws IOException {
    pipes[0].flush();
  }
}
