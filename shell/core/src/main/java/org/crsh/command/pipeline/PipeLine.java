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

package org.crsh.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.command.CommandInvoker;
import org.crsh.text.Chunk;

import java.io.IOException;

public class PipeLine implements CommandInvoker<Void, Chunk> {

  /** . */
  private final CommandInvoker[] invokers;

  /** . */
  private Pipe current;

  public PipeLine(CommandInvoker[] invokers) {
    this.invokers = invokers;
    this.current = null;
  }

  public Class<Void> getConsumedType() {
    throw new UnsupportedOperationException();
  }

  public Class<Chunk> getProducedType() {
    throw new UnsupportedOperationException();
  }

  public void open(CommandContext<? super Chunk> consumer) {
    open(0, consumer);
  }

  private CommandContext open(final int index, final CommandContext last) {
    if (index < invokers.length) {

      //
      final CommandInvoker invoker = invokers[index];
      CommandContext next = open(index + 1, last);

      //
      final Class produced = invoker.getProducedType();
      final Class<?> consumed = next.getConsumedType();
      boolean piped = index > 0;
      if (!consumed.isAssignableFrom(produced)) {
        if (produced.equals(Void.class) || consumed.equals(Void.class)) {
          // We need to check (i.e test) what happens for chunk (i.e the writer)
          PipeFilter.Sink filter = new PipeFilter.Sink(consumed, piped);
          filter.open(next);
          next = filter;
        } else if (consumed.equals(Chunk.class)) {
          PipeFilter.Chunkizer filter = new PipeFilter.Chunkizer(piped);
          filter.open((CommandContext<Chunk>)next);
          next = filter;
        } else {
          PipeFilter.Sink filter = new PipeFilter.Sink(consumed, piped);
          filter.open(next);
          next = filter;
        }
      } else {
        PipeFilter.Noop filter = new PipeFilter.Noop(piped);
        filter.open(next);
        next = filter;
      }

      //
      Pipe filterContext = new Pipe(invoker);
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

  public void close() throws IOException {
    current.close();
  }
}
