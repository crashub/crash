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

import org.crsh.command.CommandContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ScriptException;
import org.crsh.io.Filter;
import org.crsh.io.ProducerContext;
import org.crsh.text.Chunk;

import java.io.IOException;

class PipeLine implements CommandInvoker {

  /** . */
  private final CRaSHSession session;

  /** . */
  private final Filter[] pipes;

  PipeLine(CRaSHSession session, Filter[] pipes) {
    this.session = session;
    this.pipes = pipes;
  }

  void invoke(ProcessInvocationContext context) throws ScriptException, IOException {
    open(context);
    flush();
    close();
  }

  public void setSession(CommandContext session) {
    // Should we use it ?
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

  public void open(ProducerContext context) {
    ProducerContext<?> last = context;

    for (int i = pipes.length - 1;i >= 0;i--) {

      //
      ProducerContext<?> next;

      //
      Class produced = pipes[i].getProducedType();
      Class<?> consumed = last.getConsumedType();

      if (consumed.isAssignableFrom(produced)) {
        next = last;
      } else {

        // Try to adapt
        if (produced.equals(Void.class)) {
          throw new UnsupportedOperationException(produced.getSimpleName() + " -> " + consumed.getSimpleName());
        } else if (consumed.equals(Void.class)) {
          SinkPipeFilter filter = new SinkPipeFilter(consumed);
          filter.open(last);
          next = filter;
        } else if (consumed.equals(Chunk.class)) {
          ToChunkPipeFilter filter = new ToChunkPipeFilter();
          filter.open((ProducerContext<Chunk>)last);
          next = filter;
        } else {
          SinkPipeFilter filter = new SinkPipeFilter(consumed);
          filter.open(last);
          next = filter;
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
