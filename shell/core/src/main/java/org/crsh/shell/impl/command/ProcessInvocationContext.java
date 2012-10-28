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

import org.crsh.io.IOContext;
import org.crsh.io.ProducerContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;
import org.crsh.text.ChunkBuffer;

import java.io.IOException;
import java.util.Map;

class ProcessInvocationContext implements ProducerContext<Object> {

  /** . */
  private final CRaSHSession session;

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private final ChunkAdapter adapter;

  ProcessInvocationContext(CRaSHSession session, final ShellProcessContext processContext) {

    // We use this chunk buffer to buffer stuff
    // but also because it optimises the chunks
    // which provides better perormances on the client
    final ChunkBuffer buffer = new ChunkBuffer(processContext);

    //
    final ChunkAdapter adapter = new ChunkAdapter(new IOContext<Chunk>() {
      public int getWidth() {
        return processContext.getWidth();
      }

      public int getHeight() {
        return processContext.getHeight();
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
}
