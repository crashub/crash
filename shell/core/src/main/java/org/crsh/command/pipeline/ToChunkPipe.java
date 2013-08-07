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
import org.crsh.shell.ScreenContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;

import java.io.IOException;

/** @author Julien Viet */
public class ToChunkPipe<C, CONS extends CommandContext<? super Chunk>> extends AbstractPipe<C, Chunk, CONS> {

  /** . */
  private ChunkAdapter adapter;

  /** . */
  private Class<C> consumedType;

  public ToChunkPipe(Class<C> consumedType, boolean piped) {
    super(piped);

    //
    this.consumedType = consumedType;
  }

  @Override
  public void open(final CONS consumer) {
    super.open(consumer);

    //
    adapter = new ChunkAdapter(new ScreenContext() {
      public int getWidth() {
        return consumer.getWidth();
      }
      public int getHeight() {
        return consumer.getHeight();
      }
      public Class<Chunk> getConsumedType() {
        return Chunk.class;
      }
      public void provide(Chunk element) throws IOException {
        consumer.provide(element);
      }
      public void flush() throws IOException {
        consumer.flush();
      }
      public void write(Chunk chunk) throws IOException {
        consumer.write(chunk);
      }
    });
  }

  public void provide(C element) throws IOException {
    adapter.provide(element);
  }

  @Override
  public void flush() throws IOException {
    adapter.flush();
  }

  @Override
  public void close() throws IOException {
    adapter.flush();
    super.close();
  }

  public void write(Chunk chunk) throws IOException {
    adapter.write(chunk);
  }

  public Class<Chunk> getProducedType() {
    return Chunk.class;
  }

  public Class<C> getConsumedType() {
    return consumedType;
  }
}
