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

import org.crsh.io.ScreenContext;
import org.crsh.command.ScriptException;
import org.crsh.io.Filter;
import org.crsh.io.ProducerContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;

import java.io.IOException;

class ToChunkPipeFilter implements Filter<Object, Chunk> {

  /** . */
  private Filter<Chunk, ?> context;

  /** . */
  private ChunkAdapter ca;

  public Class<Chunk> getProducedType() {
    return Chunk.class;
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void setPiped(boolean piped) {
    context.setPiped(piped);
  }

  public void open(final ProducerContext<Chunk> context) {
    ca = new ChunkAdapter(new ScreenContext<Chunk>() {
      public int getWidth() {
        return context.getWidth();
      }
      public int getHeight() {
        return context.getHeight();
      }
      public void provide(Chunk element) throws IOException {
        ToChunkPipeFilter.this.context.provide(element);
      }
      public void flush() throws IOException {
        ToChunkPipeFilter.this.context.flush();
      }
    });

    //
    this.context = (Filter<Chunk, ?>)context;
  }

  public boolean takeAlternateBuffer() {
    return context.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() {
    return context.releaseAlternateBuffer();
  }

  public void provide(Object element) throws ScriptException, IOException {
    ca.provide(element);
  }

  public void flush() throws ScriptException, IOException {
    ca.flush();
  }

  public void close() throws ScriptException {
    context.close();
  }

  public String getProperty(String propertyName) {
    return context.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return context.readLine(msg, echo);
  }

  public int getWidth() {
    return context.getWidth();
  }

  public int getHeight() {
    return context.getHeight();
  }
}
