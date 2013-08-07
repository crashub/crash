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
import org.crsh.io.Consumer;
import org.crsh.shell.ScreenContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;
import org.crsh.text.ChunkBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

class CRaSHProcessContext implements CommandContext<Object>, Closeable {

  /** . */
  private final CRaSHSession session;

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private final ChunkAdapter adapter;

  /** . */
  private boolean useAlternateBuffer;

  CRaSHProcessContext(CRaSHSession session, final ShellProcessContext processContext) {

    // We use this chunk buffer to buffer stuff
    // but also because it optimises the chunks
    // which provides better perormances on the client
    final ChunkBuffer buffer = new ChunkBuffer(new Consumer<Chunk>() {
      public void provide(Chunk element) throws IOException {
        processContext.write(element);
      }
      public Class<Chunk> getConsumedType() {
        return Chunk.class;
      }
      public void flush() throws IOException {
        processContext.flush();
      }
    });

    //
    final ChunkAdapter adapter = new ChunkAdapter(new ScreenContext() {
      public int getWidth() {
        return processContext.getWidth();
      }

      public int getHeight() {
        return processContext.getHeight();
      }

      public void write(Chunk chunk) throws IOException {
        provide(chunk);
      }

      public Class<Chunk> getConsumedType() {
        return Chunk.class;
      }

      public void provide(Chunk element) throws IOException {
        buffer.provide(element);
      }

      public void flush() throws IOException {
        buffer.flush();
      }
    });

    //
    this.session = session;
    this.processContext = processContext;
    this.adapter = adapter;
    this.useAlternateBuffer = false;
  }

  public boolean isPiped() {
    throw new UnsupportedOperationException();
  }

  public boolean takeAlternateBuffer() throws IOException {
    return useAlternateBuffer = processContext.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return useAlternateBuffer = processContext.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return processContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return processContext.readLine(msg, echo);
  }

  public int getWidth() {
    return adapter.getWidth();
  }

  public int getHeight() {
    return adapter.getHeight();
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void write(Chunk chunk) throws IOException {
    adapter.provide(chunk);
  }

  public void provide(Object element) throws IOException {
    adapter.provide(element);
  }

  public void flush() throws IOException {
    adapter.flush();
  }

  public Map<String, Object> getSession() {
    return session;
  }

  public Map<String, Object> getAttributes() {
    return session.crash.getContext().getAttributes();
  }

  public void close() throws IOException {
    if (useAlternateBuffer) {
      releaseAlternateBuffer();
    }
  }
}
