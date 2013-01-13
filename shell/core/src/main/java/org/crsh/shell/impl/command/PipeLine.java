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
import org.crsh.io.InteractionContext;
import org.crsh.text.Chunk;
import org.crsh.util.Safe;

import java.io.IOException;

public class PipeLine implements CommandInvoker<Void, Chunk> {

  /** . */
  private final CommandInvoker[] invokers;

  /** . */
  private Pipe.Invoker current;

  PipeLine(CommandInvoker[] invokers) {
    this.invokers = invokers;
    this.current = null;
  }

  public void setSession(CommandContext session) {
    // Should we use it ?
  }

  public Class<Void> getConsumedType() {
    throw new UnsupportedOperationException();
  }

  public Class<Chunk> getProducedType() {
    throw new UnsupportedOperationException();
  }

  public void setPiped(boolean piped) {
    throw new UnsupportedOperationException("This should not be called");
  }

  public void open(InteractionContext<Chunk> context) {
    open(0, context);
  }

  private InteractionContext open(final int index, final InteractionContext last) {
    if (index < invokers.length) {

      //
      final CommandInvoker invoker = invokers[index];
      InteractionContext next = open(index + 1, last);

      //
      final Class produced = invoker.getProducedType();
      final Class<?> consumed = next.getConsumedType();

      if (!consumed.isAssignableFrom(produced)) {
        if (produced.equals(Void.class)) {
          throw new UnsupportedOperationException("Implement me " + produced.getSimpleName() + " -> " + consumed.getSimpleName());
        } else if (consumed.equals(Void.class)) {
          Pipe.Sink filter = new Pipe.Sink(consumed);
          filter.open(next);
          next = filter;
        } else if (consumed.equals(Chunk.class)) {
          Pipe.Chunkizer filter = new Pipe.Chunkizer();
          filter.open((InteractionContext<Chunk>)next);
          next = filter;
        } else {
          Pipe.Sink filter = new Pipe.Sink(consumed);
          filter.open(next);
          next = filter;
        }
      }

      //
      Pipe.Invoker filterContext = new Pipe.Invoker(invoker);
      filterContext.setPiped(index > 0);
      filterContext.open(next);

      // Save current filter in field
      // so if anything wrong happens it will be closed
      current = filterContext;

      //
      return filterContext;
    } else {
      return last;
    }
  }

  public void provide(Void element) throws IOException {
    throw new UnsupportedOperationException("This is not yet implemented");
  }

  public void flush() throws IOException {
    current.flush();
  }

  public void close() {
    current.close();
  }
}
