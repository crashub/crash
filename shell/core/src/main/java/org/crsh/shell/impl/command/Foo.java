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

import org.crsh.RenderingContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

class Foo implements InvocationContext, CommandInvoker {

  /** . */
  private PipeFilter next;

  /** . */
  private ChunkAdapter ca;

  Foo() {
  }

  public Class getProducedType() {
    throw new UnsupportedOperationException();
  }

  public Class getConsumedType() {
    throw new UnsupportedOperationException();
  }

  public void setPiped(boolean piped) {
    next.setPiped(piped);
  }

  public void open(final InvocationContext context) {
    ca = new ChunkAdapter(new RenderingContext<Chunk>() {
      public Class<Chunk> getConsumedType() {
        return Chunk.class;
      }
      public int getWidth() {
        return context.getWidth();
      }
      public int getHeight() {
        return context.getHeight();
      }
      public void provide(Chunk element) throws IOException {
        next.provide(element);
      }
      public void flush() throws IOException {
        next.flush();
      }
    });

    //
    next = (PipeFilter)context;
  }

  public void provide(Object element) throws ScriptException, IOException {
    ca.provide(element);
  }

  public void flush() throws ScriptException, IOException {
    ca.flush();
  }

  public void close() throws ScriptException {
    next.close();
  }

  public RenderPrintWriter getWriter() {
    return next.getWriter();
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    return next.resolve(s);
  }

  public Map<String, Object> getSession() {
    return next.getSession();
  }

  public Map<String, Object> getAttributes() {
    return next.getAttributes();
  }

  public String getProperty(String propertyName) {
    return next.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return next.readLine(msg, echo);
  }

  public int getWidth() {
    return next.getWidth();
  }

  public int getHeight() {
    return next.getHeight();
  }
}
